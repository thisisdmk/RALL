package com.looter.rall.ui.fullscreen

import androidx.compose.runtime.Composable
import com.looter.rall.ui.post.ImageGallery

@Composable
fun GalleryScreen(
    urls: List<String>,
    onClick: () -> Unit = {}
) {
    ImageGallery(urls, isZoomable = true) { _, _ -> onClick() }
}