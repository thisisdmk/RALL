package com.looter.rall.ui.feed

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.looter.rall.ui.post.PostCardController


@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    controller: PostCardController
) {
    FeedList(
        viewModel.feedFlow,
        controller
    )
}