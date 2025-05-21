package com.looter.rall.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RedditApiService {

    @GET("/r/all/best.json?raw_json=1&limit=50&always_show_media=1")
    suspend fun getRAllListAfter(@Query("after") lastItem: String?): retrofit2.Response<String>

    @GET("/r/{subreddit}/hot.json?raw_json=1&limit=50&always_show_media=1")
    suspend fun getSubredditPostsAfter(
        @Path("subreddit") subreddit: String,
        @Query("after") lastItem: String?
    ): retrofit2.Response<String>

    @GET("/comments/{id}.json?raw_json=1")
    suspend fun getPost(@Path("id") id: String?): retrofit2.Response<String>

    @GET("/api/morechildren.json?raw_json=1&api_type=json")
    suspend fun getMoreComments(
        @Query("link_id") postFullName: String,
        @Query("children") moreChildrenIds: String
    ): retrofit2.Response<String>
}