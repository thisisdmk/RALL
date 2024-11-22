package com.looter.rall.ui.feed

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.looter.rall.data.RedditRepository
import com.looter.rall.domain.RedditPost
import com.looter.rall.domain.toJson
import com.looter.rall.ui.post.CURRENT_POST
import com.looter.rall.ui.post.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: RedditRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val feedFlow: Flow<PagingData<RedditPost>> = Pager(PagingConfig(pageSize = 50)) { repository }
        .flow.cachedIn(viewModelScope)

    suspend fun rememberPost(post: RedditPost) {
        context.applicationContext.dataStore.edit { settings ->
            settings[CURRENT_POST] = post.toJson()
        }
    }
}