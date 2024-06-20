package com.looter.rall.ui.fullscreen

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.looter.rall.ui.feed.ListPlayerKey
import com.looter.rall.ui.postdetail.PostDetailsPlayerKey
import com.looter.rall.ui.videoplayer.LocalVideoPlayerController
import com.looter.rall.ui.videoplayer.PlayerKey
import com.looter.rall.ui.videoplayer.VideoPlayer
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

private const val ScreenPlayerKey = "onScreen"

@OptIn(UnstableApi::class)
@Composable
fun VideoScreen(
    url: String,
    mediaKey: String,
    goToPost: () -> Unit = {}
) {
    Log.i("VideoScreen", "VideoScreen($url, $mediaKey)")
    val playerState by LocalVideoPlayerController.current.rememberPlayerState()
    LaunchedEffect(url) {
        playerState.enablePlayer(PlayerKey(mediaKey, ScreenPlayerKey), url)
    }

    VideoPlayer(
        Modifier.fillMaxSize(),
        isCommentVisible = true,
        onCommentClick = {
            goToPost()
            playerState.enablePlayer(PlayerKey(mediaKey, PostDetailsPlayerKey), url)
        }
    ) {
        playerState.disablePlayer(PlayerKey(mediaKey, ScreenPlayerKey))
    }

    OnBack {
        playerState.enablePlayer(PlayerKey(mediaKey, ListPlayerKey), url)
    }
}

@Composable
fun OnBack(block: () -> Unit) {
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var backPressHandled by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    BackHandler(enabled = !backPressHandled) {
        block()
        backPressHandled = true
        coroutineScope.launch {
            awaitFrame()
            onBackPressedDispatcher?.onBackPressed()
            backPressHandled = false
        }
    }
}