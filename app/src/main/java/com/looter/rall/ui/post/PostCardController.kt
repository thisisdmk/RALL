package com.looter.rall.ui.post

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.looter.data.feed.models.RedditPost

interface PostCardController {
    fun navigateToImage(post: RedditPost)
    fun navigateToVideo(post: RedditPost)
    fun navigateToGallery(urls: List<String>)
    fun navigateToPost(post: RedditPost)
    fun navigateToSubreddit(post: RedditPost)
    fun navigateToUserPosts(username: String)
    fun openLink(link: String)

    object PostCardControllerNoop : PostCardController {
        override fun navigateToImage(post: RedditPost) {}
        override fun navigateToVideo(post: RedditPost) {}
        override fun navigateToGallery(urls: List<String>) {}
        override fun navigateToPost(post: RedditPost) {}
        override fun navigateToSubreddit(post: RedditPost) {}
        override fun navigateToUserPosts(username: String) {}
        override fun openLink(link: String) {}
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tempo")
val CURRENT_POST = stringPreferencesKey("current_post")