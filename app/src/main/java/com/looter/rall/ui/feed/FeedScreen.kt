package com.looter.rall.ui.feed

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.looter.rall.domain.RedditPost
import com.looter.rall.ui.post.PostCardController
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel(),
    navController: NavController
) {
    class PostCardControllerFeed : PostCardController {
        override fun navigateToImage(post: RedditPost) {
            viewModel.viewModelScope.launch {
                viewModel.rememberPost(post)
                navController.navigate("imageViewer/${post.postId}")
            }
        }

        override fun navigateToVideo(post: RedditPost) {
            viewModel.viewModelScope.launch {
                viewModel.rememberPost(post)
                navController.navigate("videoViewer/${post.postId}")
            }
        }

        override fun navigateToGallery(urls: List<String>) {
            val encoded = urls.joinToString(transform = ::encode)
            navController.navigate("galleryViewer/$encoded")
        }

        override fun navigateToPost(post: RedditPost) {
            viewModel.viewModelScope.launch {
                viewModel.rememberPost(post)
                navController.navigate("postDetail/${post.postId}")
            }
        }

        override fun navigateToSubreddit(post: RedditPost) {
            //todo
        }

        override fun openLink(link: String) {
            ContextCompat.startActivity(
                navController.context, Intent(Intent.ACTION_VIEW, Uri.parse(link)), null
            )
        }
    }

    FeedList(
        viewModel.feedFlow,
        controller = PostCardControllerFeed()
    )
}


private fun encode(url: String): String = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())