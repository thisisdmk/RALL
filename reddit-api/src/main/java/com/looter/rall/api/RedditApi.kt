package com.looter.rall.api

import com.looter.rall.api.models.RedditCommentJson
import com.looter.rall.api.models.RedditPostJson
import com.looter.rall.api.models.toRedditCommentJsons

import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

interface RedditApi {

    suspend fun getRAllPostsAfter(afterKey: String?): List<RedditPostJson>

    suspend fun getSubredditPostsAfter(subreddit: String, afterKey: String?): List<RedditPostJson>

    suspend fun getUserPostsAfter(username: String, afterKey: String?): List<RedditPostJson>

    suspend fun getPostAndComments(postId: String): Pair<RedditPostJson, List<RedditCommentJson>>

    suspend fun getMoreComments(
        postName: String,
        moreChildrenIds: String
    ): List<RedditCommentJson>
}

class RedditApiImpl @Inject constructor(
    private val service: RedditApiService
) : RedditApi {

    override suspend fun getRAllPostsAfter(afterKey: String?): List<RedditPostJson> =
        getPostsAfter(null, afterKey)

    override suspend fun getSubredditPostsAfter(
        subreddit: String,
        afterKey: String?
    ): List<RedditPostJson> =
        getPostsAfter(subreddit, afterKey)

    override suspend fun getUserPostsAfter(
        username: String,
        afterKey: String?
    ): List<RedditPostJson> =
        try {
            val response = service.getUserPostsAfter(username, afterKey).body()
            println("getUserPostsAfter($username, $afterKey) response: $response")
            extractRedditPostJsons(response)
        } catch (e: Exception) {
            println("Error in getUserPostsAfter: ${e.message}")
            e.printStackTrace()
            emptyList()
        }

    private suspend fun getPostsAfter(
        subreddit: String? = null,
        afterKey: String?
    ): List<RedditPostJson> {
        return try {
            val response = if (subreddit != null) {
                service.getSubredditPostsAfter(subreddit, afterKey)
            } else {
                service.getRAllListAfter(afterKey)
            }.body()
            println("getPostsAfter($subreddit, $afterKey) response: $response")
            extractRedditPostJsons(response)
        } catch (e: Exception) {
            println("Error in getPostsAfter: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getPostAndComments(postId: String): Pair<RedditPostJson, List<RedditCommentJson>> {
        return try {
            val response = service.getPost(postId).body()
            val childrenArray = JSONArray(response)
            val postData = childrenArray.getJSONObject(0)
                .getJSONObject("data")
                .getJSONArray("children")
                .getJSONObject(0)
                .getJSONObject("data")
            val post = RedditPostJson(postData)
            val comments = childrenArray.optJSONObject(1)
                ?.optJSONObject("data")
                ?.optJSONArray("children")
                .let(::toRedditCommentJsons)
            post to comments
        } catch (e: Exception) {
            println("Error in getPostAndComments: ${e.message}")
            e.printStackTrace()
            RedditPostJson(JSONObject()) to emptyList()
        }
    }

    override suspend fun getMoreComments(
        postName: String,
        moreChildrenIds: String
    ): List<RedditCommentJson> {
        return try {
            val response = service.getMoreComments(postName, moreChildrenIds).body()
            response?.let(::JSONObject)
                ?.optJSONObject("json")
                ?.optJSONObject("data")
                ?.optJSONArray("things")
                .let(::toRedditCommentJsons)
        } catch (e: Exception) {
            println("Error in getMoreComments: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}

private fun extractRedditPostJsons(response: String?): List<RedditPostJson> {
    if (response == null) return emptyList()
    return try {
        JSONObject(response)
            .optJSONObject("data")
            ?.getJSONArray("children")
            ?.map { index ->
                val child = getJSONObject(index)
                if (child?.getString("kind") == "t3") {
                    RedditPostJson(child.getJSONObject("data"))
                } else null
            }?.filterNotNull()
            .orEmpty()
    } catch (e: Exception) {
        println("Error in extractRedditPostJsons: ${e.message}")
        e.printStackTrace()
        emptyList()
    }
}