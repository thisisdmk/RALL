package com.looter.rall.ui.feedlist

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import androidx.paging.PagingState
import com.looter.data.feed.FeedRepository
import com.looter.data.feed.models.RedditPost

class FeedPagingSource(
    private val feedRepository: FeedRepository
) : PagingSource<String, RedditPost>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, RedditPost> {
        return try {
            val afterKey = params.key
            val res = feedRepository.loadFeed(afterKey)
            LoadResult.Page(
                data = res, prevKey = afterKey, nextKey = res.lastOrNull()?.redditName
            )
        } catch (e: Exception) {
            e.printStackTrace()
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<String, RedditPost>): String? = null
}