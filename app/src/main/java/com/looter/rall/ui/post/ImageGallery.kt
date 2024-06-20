package com.looter.rall.ui.post

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import net.engawapg.lib.zoomable.rememberZoomState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageGallery(
    urls: List<String>,
    modifier: Modifier = Modifier,
    isZoomable: Boolean = false,
    onClick: (page: Int, url: String) -> Unit = { _, _ -> }
) {
    val pagerState = rememberPagerState { urls.size }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
    ) { page ->
        if (isZoomable) {
            ZoomablePage(urls, page, pagerState)
        } else {
            ClickableImage(imageUrl = urls[page]) {
                onClick(page, urls[page])
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZoomablePage(
    urls: List<String>,
    page: Int,
    pagerState: PagerState
) {
    val zoomState = rememberZoomState()
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        ZoomableImage(urls[page], zoomState)
    }

    val isVisible = page == pagerState.settledPage
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            zoomState.reset()
        }
    }
}