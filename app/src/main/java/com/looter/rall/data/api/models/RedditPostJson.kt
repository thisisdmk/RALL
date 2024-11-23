package com.looter.rall.data.api.models

import android.net.Uri
import androidx.core.text.parseAsHtml
import com.looter.rall.domain.RedditMedia
import org.json.JSONObject

data class RedditPostJson(
    private val data: JSONObject
) {
    val id get() = data.getString("id")
    val title get() = data.getString("title")
    val fullName get() = data.getString("name")
    val subredditName get() = data.getString("subreddit_name_prefixed")
    val numberOfComments get() = data.getInt("num_comments").toString()
    val upvoteScore get() = data.getInt("score").toString()
    val previews: MediaPreviews? by lazy {
        data.optJSONObject("preview")
            ?.optJSONArray("images")
            ?.optJSONObject(0)
            ?.let(::extractPreviews)
    }

    val gifUrl: String?
        get() = if (endsGifv) {
            previewVideo?.let {
                it.copy(url = it.url.substring(0, it.url.length - 5) + ".mp4")
            }?.url
        } else {
            previews?.mp4Previews?.original?.url
        }

    private val previewVideo: RedditMedia? by lazy {
        data.optJSONObject("preview")
            ?.optJSONObject("reddit_video_preview")
            ?.let {
                RedditMedia(url, it.getInt("width"), it.getInt("height"))
            }
    }

    val permalink get() = data.getString("permalink").parseAsHtml().toString()
    val url get() = data.getString("url").parseAsHtml().toString()
    val isVideo get() = data.getBoolean("is_video")
    val isGallery get() = galleryData.isNotEmpty()
    val galleryData: List<GalleryMediaJson> by lazy { extractGallery(data) }

    val isText get() = url.contains(permalink)
    val isImage get() = endsJpegOrPng
    val isGif get() = endsGif || endsGifv
    val text get() = data.optString("selftext").orEmpty()
    val dashUrl get() = video?.dashUrl
    private val video: RedditVideoJson? by lazy {
        data.optJSONObject("media")
            ?.optJSONObject("reddit_video")
            ?.let(::RedditVideoJson)
    }

    private val uri by lazy { Uri.parse(url) }
    val path get() = uri.path
    val authority get() = uri.authority

    val endsJpegOrPng
        get() = path?.let {
            it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png")
        } ?: false
    val endsGif get() = path?.endsWith(".gif") ?: false
    val endsMp4 get() = path?.endsWith(".mp4") ?: false
    val endsGifv get() = path?.endsWith("gifv") ?: false
    val isImgur get() = authority?.contains("imgur.com") == true
    val isStreamable get() = authority?.contains("streamable.com") == true

}