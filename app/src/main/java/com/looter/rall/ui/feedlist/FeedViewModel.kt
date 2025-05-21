package com.looter.rall.ui.feedlist

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.looter.data.feed.FeedRepository
import com.looter.data.feed.models.RedditPost
import com.looter.data.feed.models.toJson
import com.looter.rall.ui.post.CURRENT_POST
import com.looter.rall.ui.post.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: FeedRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val feedStates = mutableMapOf<String?, Flow<PagingData<RedditPost>>>()

    fun getFeedFlow(subreddit: String? = null): Flow<PagingData<RedditPost>> {
        return feedStates.getOrPut(subreddit) {
            Pager(
                config = PagingConfig(
                    pageSize = 50,
                    enablePlaceholders = false,
                    maxSize = 200
                ),
                initialKey = null
            ) {
                FeedPagingSource(repository, subreddit)
            }.flow.cachedIn(viewModelScope)
        }
    }

    suspend fun rememberPost(post: RedditPost) {
        context.applicationContext.dataStore.edit { settings ->
            settings[CURRENT_POST] = post.toJson()
        }
    }
}