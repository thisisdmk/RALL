package com.looter.rall.videoplayer

import android.view.LayoutInflater
import android.view.View
import androidx.annotation.OptIn
import androidx.appcompat.widget.AppCompatImageButton
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.looter.rall.videoplayer.databinding.VideoPlayerViewBinding

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    modifier: Modifier = Modifier,
    gifPlayer: Boolean = false,
    isCommentVisible: Boolean = false,
    onCommentClick: () -> Unit = {},
    onGoFullScreenClick: () -> Unit = {},
    onRelease: () -> Unit = {}
) {
    val controller = LocalVideoPlayerController.current.Controller()
    val isPlaying by controller.isPlaying.collectAsState()
    val isEnded by controller.isEnded.collectAsState()
    val isMute by controller.isMute.collectAsState()

    Box {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                Log.d("VideoPlayer", "factory{}")
                val binding = VideoPlayerViewBinding.inflate(LayoutInflater.from(context))
                val playerView = binding.root.apply {
                    playButton.setOnClickListener { controller.play() }
                    replayButton.setOnClickListener { controller.play() }
                    pauseButton.setOnClickListener { controller.pause() }
                    soundOnButton.setOnClickListener { controller.mute() }
                    soundOffButton.setOnClickListener { controller.unmute() }
                    commentButton.setOnClickListener { onCommentClick() }
                    commentButton.visibility = if (isCommentVisible) View.VISIBLE else View.GONE
                    goFullButton.setOnClickListener { onGoFullScreenClick() }
                    goFullButton.visibility = if (isCommentVisible) View.GONE else View.VISIBLE

                    useController = !gifPlayer
                }
                if (gifPlayer) {
                    controller.installGifPlayer(playerView)
                } else {
                    controller.installVideoPlayer(playerView)
                }
                playerView
            },
            update = {
                Log.d("VideoPlayer", "update{}")
                if (isPlaying) {
                    it.playButton.visibility = View.GONE
                    it.replayButton.visibility = View.GONE
                    it.pauseButton.visibility = View.VISIBLE
                } else {
                    it.pauseButton.visibility = View.GONE
                    if (isEnded) {
                        it.playButton.visibility = View.GONE
                        it.replayButton.visibility = View.VISIBLE
                    } else {
                        it.replayButton.visibility = View.GONE
                        it.playButton.visibility = View.VISIBLE
                    }
                }
                if (isMute) {
                    it.soundOnButton.visibility = View.GONE
                    it.soundOffButton.visibility = View.VISIBLE
                } else {
                    it.soundOffButton.visibility = View.GONE
                    it.soundOnButton.visibility = View.VISIBLE
                }
                it.showController()
            },
            onReset = {
                Log.d("VideoPlayer", "onReset{}")
            },
            onRelease = {
                Log.d("VideoPlayer", "onRelease{}")
                onRelease()
            }
        )
    }
}

private val PlayerView.playButton: AppCompatImageButton
    get() = findViewById(R.id.button_play)
private val PlayerView.replayButton: AppCompatImageButton
    get() = findViewById(R.id.button_replay)
private val PlayerView.pauseButton: AppCompatImageButton
    get() = findViewById(R.id.button_pause)
private val PlayerView.soundOnButton: AppCompatImageButton
    get() = findViewById(R.id.button_sound_on)
private val PlayerView.soundOffButton: AppCompatImageButton
    get() = findViewById(R.id.button_sound_off)
private val PlayerView.commentButton: AppCompatImageButton
    get() = findViewById(R.id.button_comment)
private val PlayerView.goFullButton: AppCompatImageButton
    get() = findViewById(R.id.button_open_fullscreen)