package com.looter.rall.ui.videoplayer

import android.content.Context
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


val LocalVideoPlayerController = compositionLocalOf<VideoPlayerController> {
    error("VideoPlayerController is not provided for this scope.")
}

data class PlayerKey(
    val mediaKey: String,
    val screenKey: String
) {
    override fun toString(): String = "[$mediaKey,$screenKey]"
}

@UnstableApi
class VideoPlayerController(
    private val context: Context
) {
    private val _isPlaying = MutableStateFlow(true)
    private val _isMute = MutableStateFlow(false)
    private val urlPlaybackPositionMap = mutableStateMapOf<String, Long>()
    private fun rememberPlaybackPosition(key: PlayerKey) {
        Log.i(
            "Controller",
            "rememberPlaybackPosition(), $key"
        )
        urlPlaybackPositionMap[key.mediaKey] = exoPlayer.currentPosition
    }

    private fun restorePlaybackPosition(key: PlayerKey) {
        val restored = urlPlaybackPositionMap[key.mediaKey]
        Log.i(
            "Controller",
            "restorePlaybackPosition(), restored: $restored for $key"
        )
        exoPlayer.seekTo(restored ?: 0)
    }

    val exoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 1f
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            addListener(object : Player.Listener {
                override fun onVolumeChanged(volume: Float) {
                    super.onVolumeChanged(volume)
                    _isMute.value = volume == 0f
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    _isPlaying.value = isPlaying
                }
            })
        }
    }

    private fun setUpMedia(key: PlayerKey, url: String) {
        Log.i("Controller", "setUpMedia($key), current: $currentPlayer, url: $url")
        currentPlayer?.let(::rememberPlaybackPosition)
        if (key.mediaKey != currentPlayer?.mediaKey) {
            exoPlayer.stop()
            exoPlayer.setMediaItem(MediaItem.fromUri(url))
            exoPlayer.prepare()
            restorePlaybackPosition(key)
        }
    }

    private fun releasePlayer() {
        exoPlayer.stop()
        exoPlayer.release()
        urlPlaybackPositionMap.clear()
    }

    inner class Controller {
        val isPlaying: StateFlow<Boolean> = _isPlaying
        val isMute: StateFlow<Boolean> = _isMute
        fun mute() {
            exoPlayer.volume = 0f
        }

        fun unmute() {
            exoPlayer.volume = 1f
        }

        fun play() = exoPlayer.play()
        fun pause() = exoPlayer.pause()

        fun installGifPlayer(into: PlayerView) {
            into.player = exoPlayer
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            exoPlayer.playWhenReady = true
        }

        fun installVideoPlayer(into: PlayerView) {
            into.player = exoPlayer
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    private val _currentPlayerKey: MutableState<PlayerKey?> = mutableStateOf(null)
    private val currentPlayer: PlayerKey? get() = _currentPlayerKey.value

    @Stable
    inner class PlayerState {

        fun enablePlayer(key: PlayerKey, url: String) {
            Log.i("PlayerState", "enablePlayer($key), current: $currentPlayer")
            setUpMedia(key, url)
            _currentPlayerKey.value = key
            exoPlayer.play()
        }

        fun disablePlayer(key: PlayerKey) {
            Log.i("PlayerState", "disablePlayer($key), isEnabled: ${isPlayerEnabled(key)}")
            if (isPlayerEnabled(key)) {
                rememberPlaybackPosition(key)
                _currentPlayerKey.value = null
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
            }
        }

        fun isPlayerEnabled(key: PlayerKey) = currentPlayer == key
    }

    private val playerState = mutableStateOf(PlayerState())

    fun rememberPlayerState(): State<PlayerState> = playerState

    @Composable
    fun rememberPlayerStateForLazyList(listState: LazyListState): State<PlayerState> {
        val playerIsVisible by remember {
            derivedStateOf {
                val visibleKeys =
                    listState.layoutInfo.visibleItemsInfo.map(LazyListItemInfo::key)
                currentPlayer?.let { it.mediaKey in visibleKeys } ?: false
            }
        }
        LaunchedEffect(key1 = playerIsVisible) {
            if (!playerIsVisible) {
                exoPlayer.pause()
            }
        }
        return playerState
    }

    @Composable
    fun SyncWithLifecycleOwner(
        lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
        onPause: () -> Unit = exoPlayer::pause,
        onResume: () -> Unit = {},
        onDestroy: () -> Unit = ::releasePlayer
    ) = DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> onPause()
                Lifecycle.Event.ON_RESUME -> onResume()
                Lifecycle.Event.ON_DESTROY -> onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}