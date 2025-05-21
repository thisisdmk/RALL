package com.looter.rall.api

import com.looter.rall.api.models.RedditCommentJson
import com.looter.rall.api.models.RedditPostJson
import com.looter.rall.api.models.toRedditCommentJsons

import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

interface RedditApi {

    suspend fun getPostsAfter(afterKey: String?): List<RedditPostJson>

    suspend fun getPostAndComments(postId: String): Pair<RedditPostJson, List<RedditCommentJson>>

    suspend fun getMoreComments(
        postName: String,
        moreChildrenIds: String
    ): List<RedditCommentJson>
}

class RedditApiImpl @Inject constructor(
    private val service: RedditApiService
) : RedditApi {

    override suspend fun getPostsAfter(afterKey: String?): List<RedditPostJson> {
        val response = service.getPostListAfter(afterKey).body()
        println("getPostsAfter($afterKey) response: $response")
        return extractRedditPostJsons(response)
    }

    override suspend fun getPostAndComments(postId: String): Pair<RedditPostJson, List<RedditCommentJson>> {
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
                .let(::toRedditCommentJsons)

        return post to comments
    }

    override suspend fun getMoreComments(
        postName: String,
        moreChildrenIds: String
    ): List<RedditCommentJson> {
        val response = service.getMoreComments(postName, moreChildrenIds).body()
        return response?.let(::JSONObject)
            ?.optJSONObject("json")
            ?.optJSONObject("data")
            ?.optJSONArray("things")
            .let(::toRedditCommentJsons)
    }
}

private fun extractRedditPostJsons(response: String?): List<RedditPostJson> {
    if (response == null) return emptyList()
    return JSONObject(response)
        .optJSONObject("data")
        ?.getJSONArray("children")
        ?.map { index ->
            val child = getJSONObject(index)
            if (child?.getString("kind") == "t3") {
                RedditPostJson(child.getJSONObject("data"))
            } else null
        }?.filterNotNull()
        .orEmpty()
}