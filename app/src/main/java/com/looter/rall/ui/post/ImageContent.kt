package com.looter.rall.ui.post

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import com.looter.rall.ui.videoplayer.Loading
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.fresco.FrescoImage
import com.skydoves.landscapist.fresco.FrescoImageState
import net.engawapg.lib.zoomable.ZoomState
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun ClickableImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FrescoImage(
        imageUrl = imageUrl,
        imageOptions = ImageOptions(
            contentScale = ContentScale.FillWidth
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick() }),
        failure = { ImageFailure(imageUrl) },
        loading = { Loading(Modifier.fillMaxSize()) }
    )
}

@Composable
fun ZoomableImage(
    imageUrl: String,
    zoomState: ZoomState = rememberZoomState()
) {
    FrescoImage(
        imageUrl = imageUrl,
        imageOptions = ImageOptions(
            contentScale = ContentScale.FillWidth
        ),
        modifier = Modifier
            .fillMaxSize()
            .zoomable(zoomState),
        failure = { ImageFailure(imageUrl) },
        loading = { Loading(Modifier.fillMaxSize()) },
        onImageStateChanged = { state ->
            state.getImageSizePx()?.toSize()?.let(zoomState::setContentSize)
        }
    )
}

@Composable
private fun ImageFailure(imageUrl: String) {
    Text(text = "Image load failure $imageUrl")
}

private fun FrescoImageState.getImageSizePx(): IntSize? =
    (this as? FrescoImageState.Success)?.imageBitmap
        ?.let {
            IntSize(it.width, it.height)
        }