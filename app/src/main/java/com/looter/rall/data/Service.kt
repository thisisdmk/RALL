package com.looter.rall.data

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface Service {

    @GET("/r/all/best.json?raw_json=1&limit=10&always_show_media=1")
    suspend fun getPostListAfter(@Query("after") lastItem: String?): Response<String>

    @GET("/comments/{id}.json?raw_json=1")
    suspend fun getPost(@Path("id") id: String?): Response<String>

    @GET("/api/morechildren.json?raw_json=1&api_type=json")
    suspend fun getMoreComments(@Query("link_id") postFullName: String, @Query("children") moreChildrenIds: String): Response<String>
}