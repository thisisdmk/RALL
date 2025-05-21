package com.looter.rall.ui.feedlist

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.looter.data.feed.models.RedditPost
import com.looter.rall.ui.lazyItemsIndexed
import com.looter.rall.ui.post.PostCard
import com.looter.rall.ui.post.PostCardController
import com.looter.rall.videoplayer.LocalVideoPlayerController
import kotlinx.coroutines.flow.Flow

const val ListPlayerKey = "onList"

@kotlin.OptIn(ExperimentalMaterial3AdaptiveApi::class)
@OptIn(UnstableApi::class)
@Composable
fun FeedList(
    flow: Flow<PagingData<RedditPost>>,
    controller: PostCardController = PostCardController.PostCardControllerNoop,
    subreddit: String? = null
) {
    val lazyPagingFeed = flow.collectAsLazyPagingItems()
    val lazyListState = rememberLazyListState()
    val playerState =
        LocalVideoPlayerController.current.rememberPlayerStateForLazyList(lazyListState)
    val windowSize = currentWindowSize()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
    ) {
        subreddit?.let {
            Text(
                text = if (it.startsWith("user/")) "u/${it.removePrefix("user/")}" else "r/$it",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            lazyItemsIndexed(
                items = lazyPagingFeed,
                key = { _, item -> item.redditName },
                itemContent = { _, item ->
                    val isUserFeed = subreddit?.startsWith("user/") == true
                    val isSubredditFeed = subreddit != null && !isUserFeed
                    PostCard(
                        item = item,
                        screenKey = ListPlayerKey,
                        controller = controller,
                        playerState = playerState,
                        windowSize = windowSize,
                        hideSubreddit = isSubredditFeed,
                        showUsername = isSubredditFeed
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            )
        }
    }
}