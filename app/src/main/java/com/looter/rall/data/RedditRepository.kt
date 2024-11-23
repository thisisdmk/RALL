package com.looter.rall.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.looter.rall.data.api.RedditApi
import com.looter.rall.data.api.models.RedditCommentJson
import com.looter.rall.data.api.models.RedditPostJson
import com.looter.rall.domain.CommentItem
import com.looter.rall.domain.CommentTree
import com.looter.rall.domain.GalleryItem
import com.looter.rall.domain.PostContent
import com.looter.rall.domain.PostContent.Gallery
import com.looter.rall.domain.PostContent.Image
import com.looter.rall.domain.PostContent.Link
import com.looter.rall.domain.PostContent.Text
import com.looter.rall.domain.PostContent.Video
import com.looter.rall.domain.RedditPost
import javax.inject.Inject

class RedditRepository @Inject constructor(
    private val redditApi: RedditApi,
) : PagingSource<String, RedditPost>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, RedditPost> {
        return try {
            val afterKey = params.key
            val res = redditApi.getPostsAfter(afterKey).mapNotNull { jsonToPost(it) }
            LoadResult.Page(
                data = res, prevKey = afterKey, nextKey = res.lastOrNull()?.redditName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    private fun jsonToPost(json: RedditPostJson): RedditPost? {
        val type = getContent(json) ?: return null
        return RedditPost(
            json.title.trim(),
            json.subredditName,
            json.numberOfComments,
            json.upvoteScore,
            type,
            json.id
        )
    }

    private fun getContent(post: RedditPostJson): PostContent? {
        return if (post.isVideo) {
            videoContent(post)
        } else if (post.isGallery) {
            galleryContent(post)
        } else if (post.isGif) {
            gifContent(post)
        } else if (post.isImage) {
            imageContent(post)
        } else if (post.isText) {
            Text(post.text)
        } else {
            linkContent(post)
        }
    }

    private fun videoContent(post: RedditPostJson): Video? {
        val dashUrl = post.dashUrl
        if (dashUrl.isNullOrBlank()) return null
        return Video(
            url = dashUrl,
            thumbnailUrls = post.previews?.resolutions.orEmpty(),
            asGif = false
        )
    }

    private fun galleryContent(post: RedditPostJson): Gallery? {
        val items = post.galleryData.mapNotNull {
            if (it.original != null) {
                GalleryItem(
                    original = it.original,
                    previews = it.previews,
                    caption = it.caption,
                    outboundUrl = it.outboundUrl
                )
            } else null
        }
        return if (items.isEmpty()) null else Gallery(items)
    }

    private fun gifContent(post: RedditPostJson): Video? {
        val gifUrl = post.gifUrl
        if (gifUrl.isNullOrBlank()) return null
        return Video(
            url = gifUrl,
            thumbnailUrls = post.previews?.resolutions.orEmpty(),
            asGif = true
        )
    }

    private fun imageContent(post: RedditPostJson): Image? {
        val original = post.previews?.original ?: return null
        return Image(original, post.previews?.resolutions.orEmpty())
    }

    private fun linkContent(post: RedditPostJson): Link? {
        val resolutions = post.previews?.resolutions
        if (resolutions.isNullOrEmpty()) return null
        val orig = post.previews?.original
        val previews = if (orig != null) resolutions + orig else resolutions
        return Link(post.url, post.text, previews)
    }

    override fun getRefreshKey(state: PagingState<String, RedditPost>): String? = null

    suspend fun getPostDetails(postId: String): Pair<RedditPost, List<CommentTree>> {
        val response = redditApi.getPostAndComments(postId)
        val postJson = response.first
        val comments = response.second
        return jsonToPost(postJson)!! to comments.toCommentTree()
    }

    suspend fun getMoreComments(
        parentName: String,
        postName: String,
        moreChildrenIds: String
    ): List<CommentTree> {
        val response2 = redditApi.getMoreComments(postName, moreChildrenIds)
        val map = response2.groupBy(RedditCommentJson::parentId)
        return map[parentName]?.toCommentTree { c ->
            map[c.fullName].orEmpty()
        }.orEmpty()
    }

    private fun List<RedditCommentJson>.toCommentTree(getChildren: (RedditCommentJson) -> List<RedditCommentJson> = { it.replies }): List<CommentTree> =
        mapNotNull { item ->
            if (item.isMore) {
                if (item.moreCount == 0) {
                    null
                } else {
                    CommentItem(
                        id = item.id,
                        text = "Load more: ${item.moreCount}",
                        depth = item.depth,
                        url = null,
                        name = item.fullName,
                        parentName = item.parentId,
                        isMoreDetails = true,
                        moreChildrenIds = item.moreChildrenIds
                    ).let(::CommentTree)
                }
            } else {
                CommentItem(
                    id = item.id,
                    text = item.rawText,
                    depth = item.depth,
                    url = item.mediaMeta?.url,
                    author = item.author,
                    name = item.fullName,
                    parentName = item.parentId
                ).let {
                    CommentTree(it, getChildren(item).toCommentTree(getChildren).toMutableList())
                }
            }
        }
}