package com.looter.rall.ui.postdetail

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.looter.rall.domain.PostContent
import com.looter.rall.ui.feed.ListPlayerKey
import com.looter.rall.ui.fullscreen.OnBack
import com.looter.rall.ui.post.PostCard
import com.looter.rall.ui.videoplayer.LocalVideoPlayerController
import com.looter.rall.ui.videoplayer.PlayerKey

const val PostDetailsPlayerKey = "onDetails"

@OptIn(UnstableApi::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val details = viewModel.postDetails.collectAsState()
    val comments = viewModel.commentList.collectAsState()
    val playerState = LocalVideoPlayerController.current.rememberPlayerState()
    
    CommentList(
        { PostCard(details.value, PostDetailsPlayerKey, playerState = playerState) },
        comments.value,
        viewModel.collapsedState,
        viewModel.loadingMoreState,
        viewModel::commentClicked
    )

    OnBack {
        val post = details.value
        post.type.let { it as? PostContent.Video }?.let {
            playerState.value.enablePlayer(PlayerKey(post.redditName, ListPlayerKey), it.url)
        }
    }
}