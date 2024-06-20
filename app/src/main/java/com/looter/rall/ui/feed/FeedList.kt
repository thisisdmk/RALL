package com.looter.rall.ui.feed

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.media3.common.util.UnstableApi
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.looter.rall.domain.RedditPost
import com.looter.rall.ui.lazyItemsIndexed
import com.looter.rall.ui.post.PostCard
import com.looter.rall.ui.post.PostCardController
import com.looter.rall.ui.videoplayer.LocalVideoPlayerController
import kotlinx.coroutines.flow.Flow

const val ListPlayerKey = "onList"

@kotlin.OptIn(ExperimentalMaterial3AdaptiveApi::class)
@OptIn(UnstableApi::class)
@Composable
fun FeedList(
    flow: Flow<PagingData<RedditPost>>,
    controller: PostCardController = PostCardController.PostCardControllerNoop
) {
    val lazyPagingFeed = flow.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()
    val playerState =
        LocalVideoPlayerController.current.rememberPlayerStateForLazyList(lazyListState)
    val windowSize = currentWindowSize()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        lazyItemsIndexed(
            items = lazyPagingFeed,
            key = { _, item -> item.redditName },
            itemContent = { _, item ->
                PostCard(item, ListPlayerKey, controller, playerState, windowSize)
                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary)
            })
    }
}