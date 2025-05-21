package com.looter.rall.ui.feedlist

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.looter.data.feed.FeedRepository
import com.looter.data.feed.models.RedditPost

class FeedPagingSource(
    private val feedRepository: FeedRepository,
    private val subreddit: String? = null
) : PagingSource<String, RedditPost>() {

    private suspend fun loadFeed(afterKey: String?): List<RedditPost> = if (subreddit != null) {
        feedRepository.loadSubredditFeed(subreddit, afterKey)
    } else {
        feedRepository.loadRAllFeed(afterKey)
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, RedditPost> {
        return try {
            val afterKey = params.key
            val res = loadFeed(afterKey)
            LoadResult.Page(
                data = res,
                prevKey = null,
                nextKey = res.lastOrNull()?.postKey
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, RedditPost>): String? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.data?.lastOrNull()?.postKey
        }
}