package com.looter.rall.ui.post

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.looter.data.feed.models.RedditPost
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class DefaultPostCardController(
    private val navController: NavController,
    private val coroutineScope: CoroutineScope,
    private val onPostRemember: (suspend (RedditPost) -> Unit)? = null,
    private val skipPostNavigation: Boolean = false
) : PostCardController {

    override fun navigateToImage(post: RedditPost) {
        coroutineScope.launch {
            onPostRemember?.invoke(post)
            navController.navigate("imageViewer/${post.postId}")
        }
    }

    override fun navigateToVideo(post: RedditPost) {
        coroutineScope.launch {
            onPostRemember?.invoke(post)
            navController.navigate("videoViewer/${post.postId}")
        }
    }

    override fun navigateToGallery(urls: List<String>) {
        val encoded = urls.joinToString(transform = { URLEncoder.encode(it, StandardCharsets.UTF_8.toString()) })
        navController.navigate("galleryViewer/$encoded")
    }

    override fun navigateToPost(post: RedditPost) {
        if (!skipPostNavigation) {
            coroutineScope.launch {
                onPostRemember?.invoke(post)
                navController.navigate("postDetail/${post.postId}")
            }
        }
    }

    override fun navigateToSubreddit(post: RedditPost) {
        post.subredditName.let { subredditName ->
            navController.navigate("subreddit/$subredditName")
        }
    }

    override fun openLink(link: String) {
        ContextCompat.startActivity(
            navController.context,
            Intent(Intent.ACTION_VIEW, Uri.parse(link)),
            null
        )
    }
} 