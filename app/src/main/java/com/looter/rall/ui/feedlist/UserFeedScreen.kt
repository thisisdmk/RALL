package com.looter.rall.ui.feedlist

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.looter.rall.ui.post.DefaultPostCardController

@Composable
fun UserFeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    navController: NavController,
    username: String
) {
    FeedList(
        viewModel.getFeedFlow("user/$username"),
        controller = DefaultPostCardController(
            navController = navController,
            coroutineScope = viewModel.viewModelScope,
            onPostRemember = { post -> viewModel.rememberPost(post) }
        ),
        subreddit = "user/$username"
    )
} 