package com.looter.rall.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.looter.rall.domain.CommentItem
import com.looter.rall.domain.CommentTree
import com.looter.rall.domain.GalleryItem
import com.looter.rall.domain.PostContent
import com.looter.rall.domain.PostContent.Gallery
import com.looter.rall.domain.PostContent.Image
import com.looter.rall.domain.PostContent.Link
import com.looter.rall.domain.PostContent.Text
import com.looter.rall.domain.PostContent.Video
import com.looter.rall.domain.RedditCommentJson
import com.looter.rall.domain.RedditPost
import com.looter.rall.domain.RedditPostJson
import com.looter.rall.domain.toRedditComments
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class RedditRepository @Inject constructor(
    private val service: Service
) : PagingSource<String, RedditPost>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, RedditPost> {
        return try {
            val afterKey = params.key
            val response = service.getPostListAfter(afterKey).body()

            val res = parsePostsSync(response)?.toList().orEmpty().map {
                RedditPost(
                    it.title,
                    it.subredditName,
                    it.numberOfComments,
                    it.upvoteScore,
                    getContent(it),
                    it.id
                )
            }
            LoadResult.Page(
                data = res, prevKey = afterKey, nextKey = res.lastOrNull()?.redditName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    private fun getContent(post: RedditPostJson): PostContent {
        return if (post.isVideo) {
            Video(
                url = post.dashUrl.orEmpty(),
                thumbnailUrls = post.previews?.resolutions.orEmpty(),
                asGif = false
            )
        } else if (post.isGallery) {
            Gallery(post.galleryData.map {
                GalleryItem(
                    original = it.original,
                    previews = it.previews,
                    caption = it.caption,
                    outboundUrl = it.outboundUrl
                )
            })
        } else if (post.isGif) {
            Video(
                url = post.gifUrl!!,
                thumbnailUrls = post.previews?.resolutions.orEmpty(),
                asGif = true
            )
        } else if (post.isImage) {
            Image(post.previews?.original!!, post.previews?.resolutions.orEmpty())
        } else if (post.isText) {
            Text(post.text)
        } else {
            Link(
                post.url, post.text,
                post.previews?.resolutions.orEmpty().plus(post.previews?.original!!)//todo npe
            )
        }
    }

    override fun getRefreshKey(state: PagingState<String, RedditPost>): String? = null

    suspend fun getPostDetails(postId: String): Pair<RedditPost, List<CommentTree>> {
        val response = service.getPost(postId).body()

        val childrenArray = JSONArray(response)
        val postData = childrenArray.getJSONObject(0)
            .getJSONObject("data")
            .getJSONArray("children")
            .getJSONObject(0)
            .getJSONObject("data")
        val post = RedditPostJson(postData)
        val comments =
            childrenArray.optJSONObject(1)
                ?.optJSONObject("data")
                ?.optJSONArray("children")
                .let(::toRedditComments)
                .toCommentTree()

        return RedditPost(
            title = post.title,
            subredditName = post.subredditName, numberOfComments = post.numberOfComments,
            upvoteScore = post.upvoteScore, postId = post.id,
            type = getContent(post)
        ) to comments
    }

    suspend fun getMoreComments(
        parentName: String,
        postName: String,
        moreChildrenIds: String
    ): List<CommentTree> {
        val response = service.getMoreComments(postName, moreChildrenIds).body()
        return response?.let(::JSONObject)
            ?.optJSONObject("json")
            ?.optJSONObject("data")
            ?.optJSONArray("things")
            .let(::toRedditComments)
            .let {
                val map = it.groupBy(RedditCommentJson::parentId)
                map[parentName]?.toCommentTree { c ->
                    map[c.fullName].orEmpty()
                }.orEmpty()
            }
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
                    name = item.fullName,
                    parentName = item.parentId
                ).let {
                    CommentTree(it, getChildren(item).toCommentTree(getChildren).toMutableList())
                }
            }
        }
}

fun parsePostsSync(response: String?): LinkedHashSet<RedditPostJson>? {
    val newPosts: LinkedHashSet<RedditPostJson> = LinkedHashSet()
    return try {
        val jsonResponse = JSONObject(response!!)
        val dataChildren = jsonResponse.getJSONObject("data").getJSONArray("children")

        for (i in 0 until dataChildren.length()) {
            if (dataChildren.getJSONObject(i).getString("kind") == "t3") {
                val postData = dataChildren.getJSONObject(i).getJSONObject("data")
                newPosts.add(RedditPostJson(postData))
            }
        }
        newPosts
    } catch (e: JSONException) {
        e.printStackTrace()
        null
    }
}