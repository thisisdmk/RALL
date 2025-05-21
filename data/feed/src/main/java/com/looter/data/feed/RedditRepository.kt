package com.looter.data.feed

import com.looter.data.feed.models.CommentItem
import com.looter.data.feed.models.CommentTree
import com.looter.data.feed.models.RedditPost
import com.looter.rall.api.RedditApi
import com.looter.rall.api.models.RedditCommentJson
import javax.inject.Inject
import kotlin.collections.groupBy
import kotlin.collections.mapNotNull
import kotlin.collections.orEmpty
import kotlin.collections.toMutableList
import kotlin.let
import kotlin.to

class RedditRepository @Inject constructor(
    private val redditApi: RedditApi,
) {

    suspend fun getPostDetails(postId: String): Pair<RedditPost, List<CommentTree>> {
        val response = redditApi.getPostAndComments(postId)
        val postJson = response.first
        val comments = response.second
        return toRedditPost(postJson)!! to comments.toCommentTree()
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