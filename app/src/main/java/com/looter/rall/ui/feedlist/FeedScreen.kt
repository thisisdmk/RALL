package com.looter.rall.ui.feedlist

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.looter.rall.ui.post.DefaultPostCardController
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    navController: NavController,
    subreddit: String? = null
) {
    FeedList(
        viewModel.getFeedFlow(subreddit),
        controller = DefaultPostCardController(
            navController = navController,
            coroutineScope = viewModel.viewModelScope,
            onPostRemember = { post -> viewModel.rememberPost(post) }
        ),
        subreddit
    )
}


private fun encode(url: String): String = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())