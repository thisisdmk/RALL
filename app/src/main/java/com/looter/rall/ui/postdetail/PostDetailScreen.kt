package com.looter.rall.ui.postdetail

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.looter.data.feed.models.PostContent
import com.looter.rall.ui.feedlist.ListPlayerKey
import com.looter.rall.ui.fullscreen.GalleryScreen
import com.looter.rall.ui.fullscreen.ImageScreen
import com.looter.rall.ui.fullscreen.OnBack
import com.looter.rall.ui.fullscreen.VideoScreen
import com.looter.rall.ui.post.PostCard
import com.looter.rall.videoplayer.LocalVideoPlayerController
import com.looter.rall.videoplayer.PlayerKey

const val PostDetailsPlayerKey = "onDetails"

@OptIn(UnstableApi::class)
@Composable
fun PostDetailScreen(
    viewModel: PostDetailViewModel = hiltViewModel(),
    isFullScreen: Boolean = false
) {
    val details = viewModel.postDetails.collectAsState()
    val comments = viewModel.commentList.collectAsState()
    val playerState = LocalVideoPlayerController.current.rememberPlayerState()
    val isFullScreenState: MutableState<Boolean> = remember { mutableStateOf(isFullScreen) }

    if (isFullScreenState.value) {
        when (val content = details.value.type) {
            is PostContent.Video -> {
                VideoScreen(content.url, details.value.redditName) {
                    isFullScreenState.value = false
                }
            }

            is PostContent.Gallery -> {
                GalleryScreen(content.originalUrls)
            }

            is PostContent.Image -> {
                ImageScreen(content.original.url)
            }

            else -> {

            }
        }
    } else {
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
}