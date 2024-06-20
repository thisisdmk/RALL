package com.looter.rall.ui.videoplayer

import androidx.annotation.OptIn
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.looter.rall.R
import com.looter.rall.ui.videoplayer.VideoPlayerController.PlayerState
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.fresco.FrescoImage


private const val DefaultScreenKey = "key"

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerWithThumbnail(
    url: String,
    thumbnailUrl: String,
    mediaKey: String,
    modifier: Modifier = Modifier,
    screenKey: String = DefaultScreenKey,
    playerState: State<PlayerState> = LocalVideoPlayerController.current.rememberPlayerState(),
    isGifPlayer: Boolean = false,
    onGoFullScreenClick: () -> Unit = {}
) {
    val playerKey = remember { PlayerKey(mediaKey, screenKey) }

    Crossfade(
        targetState = playerState.value.isPlayerEnabled(playerKey),
        animationSpec = tween(durationMillis = 700),
        label = "ThumbnailToPlayer"
    ) { isPlayer ->
        if (isPlayer) {
            Log.i("new VideoPlayer", "new player: $playerKey, gif: $isGifPlayer")
            VideoPlayer(modifier, isGifPlayer, onGoFullScreenClick = onGoFullScreenClick) {
                playerState.value.disablePlayer(playerKey)
            }
        } else {
            Thumbnail(thumbnailUrl, modifier, onGoFullScreenClick) {
                playerState.value.enablePlayer(playerKey, url)
            }
        }
    }
}

@Composable
fun Thumbnail(
    thumbnailUrl: String,
    modifier: Modifier = Modifier,
    onThumbnailClick: () -> Unit,
    onPlayClicked: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        FrescoImage(
            imageUrl = thumbnailUrl,
            imageOptions = ImageOptions(
                contentScale = ContentScale.FillWidth
            ),
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onThumbnailClick),
            failure = {
                Text(text = "Thumbnail load failure $thumbnailUrl")
            },
            loading = { Loading(Modifier.fillMaxSize()) }
        )
        TextButton(
            modifier = Modifier
                .align(Alignment.Center)
                .size(80.dp),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.textButtonColors(Color(0, 0, 0, 10)),
            onClick = onPlayClicked
        ) {
            Icon(
                painter = painterResource(R.drawable.icon_play_circle),
                modifier = Modifier.size(80.dp),
                tint = Color.White,
                contentDescription = stringResource(R.string.video_player_thumbnail_play_button_content_description)
            )
        }
    }
}

@Composable
fun Loading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(50.dp),
            strokeWidth = 4.dp
        )
    }
}