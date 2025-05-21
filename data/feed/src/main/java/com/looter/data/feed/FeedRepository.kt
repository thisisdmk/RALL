package com.looter.data.feed

import com.looter.data.feed.models.RedditPost
import com.looter.rall.api.RedditApi
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val redditApi: RedditApi,
) {

    suspend fun loadFeed(afterKey: String?): List<RedditPost> =
        redditApi.getPostsAfter(afterKey).mapNotNull { toRedditPost(it) }

}