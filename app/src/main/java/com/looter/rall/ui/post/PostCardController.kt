package com.looter.rall.ui.post

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.looter.rall.domain.RedditPost
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

interface PostCardController {
    fun navigateToImage(url: String)
    fun navigateToVideo(key: String, url: String, postId: String)
    fun navigateToGallery(urls: List<String>)
    fun navigateToPost(post: RedditPost)
    fun navigateToSubreddit(post: RedditPost)
    fun openLink(link: String)

    object PostCardControllerNoop : PostCardController {
        override fun navigateToImage(url: String) {}
        override fun navigateToVideo(key: String, url: String, postId: String) {}
        override fun navigateToGallery(urls: List<String>) {}
        override fun navigateToPost(post: RedditPost) {}
        override fun navigateToSubreddit(post: RedditPost) {}
        override fun openLink(link: String) {}
    }
}

class PostCardControllerImpl(
    private val navController: NavController,
    private val context: Context
) : PostCardController {

    override fun navigateToImage(url: String) {
        navController.navigate("imageViewer/${encode(url)}")
    }

    override fun navigateToVideo(key: String, url: String, postId: String) {
        navController.navigate("videoViewer/${encode(url)}/$key/$postId")
    }

    override fun navigateToGallery(urls: List<String>) {
        val encoded = urls.joinToString(transform = ::encode)
        navController.navigate("galleryViewer/$encoded")
    }

    override fun navigateToPost(post: RedditPost) {
        navController.navigate("postDetail/${post.postId}")
    }

    override fun navigateToSubreddit(post: RedditPost) {
        //todo
    }

    override fun openLink(link: String) {
        ContextCompat.startActivity(
            context, Intent(Intent.ACTION_VIEW, Uri.parse(link)), null
        )
    }
}

private fun encode(url: String): String = URLEncoder.encode(url, StandardCharsets.UTF_8.toString())