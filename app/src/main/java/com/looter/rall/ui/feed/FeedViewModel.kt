package com.looter.rall.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.looter.rall.data.RedditRepository
import com.looter.rall.domain.RedditPost
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@HiltViewModel
class FeedViewModel @Inject constructor(
    private val repository: RedditRepository
) : ViewModel() {

    val feedFlow: Flow<PagingData<RedditPost>> = Pager(PagingConfig(pageSize = 50)) { repository }
        .flow.cachedIn(viewModelScope)

}