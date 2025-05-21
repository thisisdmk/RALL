package com.looter.data.feed

import com.looter.data.feed.models.RedditPost
import com.looter.rall.api.RedditApi
import javax.inject.Inject

class FeedRepository @Inject constructor(
    private val redditApi: RedditApi,
) {

    suspend fun loadRAllFeed(afterKey: String?): List<RedditPost> =
        redditApi.getRAllPostsAfter(afterKey).mapNotNull { toRedditPost(it) }

    suspend fun loadSubredditFeed(subreddit: String, afterKey: String?): List<RedditPost> =
        redditApi.getSubredditPostsAfter(subreddit, afterKey).mapNotNull { toRedditPost(it) }
}