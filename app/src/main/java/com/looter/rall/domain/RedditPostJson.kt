package com.looter.rall.domain

import android.net.Uri
import androidx.core.text.parseAsHtml
import org.json.JSONArray
import org.json.JSONObject

data class RedditMedia(
    val url: String, val width: Int, val height: Int
)

data class MediaPreviews(
    val original: RedditMedia,
    val resolutions: List<RedditMedia> = emptyList(),
    val gifPreviews: MediaPreviews? = null,
    val mp4Previews: MediaPreviews? = null,
)

fun mediaFromPreview(json: JSONObject): RedditMedia =
    RedditMedia(json.getString("url"), json.getInt("width"), json.getInt("height"))

fun mediaFromMeta(json: JSONObject): RedditMedia =
    RedditMedia(json.getString("u"), json.getInt("x"), json.getInt("y"))

fun mediaFromMeta(previewsArray: JSONArray): List<RedditMedia> =
    previewsArray.map { mediaFromMeta(getJSONObject(it)) }

data class GalleryMediaItem(
    private val mediaId: String,
    private val galleryDataItem: JSONObject,
    private val itemMeta: JSONObject
) {
    val id: Long = galleryDataItem.getLong("id")
    val caption: String? = galleryDataItem.optString("caption")
    val outboundUrl: String? = galleryDataItem.optString("outbound_url")

    val type: String = itemMeta.getString("e")// Image, AnimatedImage
    val mime: String = itemMeta.getString("m")// image/jpg, image/gif
    val previews: List<RedditMedia> = mediaFromMeta(itemMeta.getJSONArray("p"))
    val original: RedditMedia = mediaFromMeta(itemMeta.getJSONObject("s"))
}

//"media": {
//    "reddit_video": {
//        "bitrate_kbps": 2400,
//        "fallback_url": "https://v.redd.it/rwvldua6d0nc1/DASH_720.mp4?source=fallback",
//        "has_audio": true,
//        "height": 794,
//        "width": 720,
//        "scrubber_media_url": "https://v.redd.it/rwvldua6d0nc1/DASH_96.mp4",
//        "dash_url": "https://v.redd.it/rwvldua6d0nc1/DASHPlaylist.mpd?a=1712481048%2CMDBkNDE2MGM5YzVkMWM5YTFmMzRiMzBiNmFmODFjMTJmY2IyOTNmOTE1NzBiYzc2OGU4Njk5MzFkYjY0MjEwZA%3D%3D&v=1&f=sd",
//        "duration": 6,
//        "hls_url": "https://v.redd.it/rwvldua6d0nc1/HLSPlaylist.m3u8?a=1712481048%2CZjI5YjQwNmEzZmY0M2IyYzkzNjFkMjE1YTNjNTc3NWU2MTg4Y2VlMDk3ODBmYjY0NTJhMjcwOWUwNzBkMjU2MA%3D%3D&v=1&f=sd",
//        "is_gif": false,
//        "transcoding_status": "completed"
//    }
//}

data class RedditVideoJson(
    private val redditVideo: JSONObject
) {
    val hlsUrl get() = redditVideo.getString("hls_url").parseAsHtml().toString()
    val fallbackUrl get() = redditVideo.getString("fallback_url")
    val dashUrl get() = redditVideo.getString("dash_url")
    val height get() = redditVideo.getInt("height")
    val width get() = redditVideo.getInt("width")
}

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
    val previewVideo: RedditMedia? by lazy {
        data.optJSONObject("preview")
            ?.optJSONObject("reddit_video_preview")
            ?.let {
                RedditMedia(url, it.getInt("width"), it.getInt("height"))
            }
    }

    val video: RedditVideoJson? by lazy {
        data.optJSONObject("media")
            ?.optJSONObject("reddit_video")
            ?.let(::RedditVideoJson)
    }

    val gifUrl: String?
        get() = if (endsGifv) {
            previewVideo?.let {
                it.copy(url = it.url.substring(0, it.url.length - 5) + ".mp4")
            }?.url
        } else {
            previews?.mp4Previews?.original?.url
        }

    val permalink get() = data.getString("permalink").parseAsHtml().toString()
    val url get() = data.getString("url").parseAsHtml().toString()
    val isVideo get() = data.getBoolean("is_video")
    val isGallery get() = galleryData.isNotEmpty()
    val isText get() = url.contains(permalink)
    val isImage get() = endsJpegOrPng
    val isGif get() = endsGif || endsGifv
    val text get() = data.optString("selftext").orEmpty()
    val dashUrl get() = video?.dashUrl

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

    val galleryData: List<GalleryMediaItem> by lazy { extractGallery() }

    private fun extractGallery(): List<GalleryMediaItem> {
        val galleryDataItems = data.optJSONObject("gallery_data")?.optJSONArray("items")
        if (galleryDataItems != null && galleryDataItems.length() > 0) {
            val metadata = data.getJSONObject("media_metadata")
            return galleryDataItems.map { i ->
                val galleryItem = getJSONObject(i)
                val mediaId = galleryItem.getString("media_id")
                val itemMeta = metadata.getJSONObject(mediaId)
                GalleryMediaItem(mediaId, galleryItem, itemMeta)
            }
        }
        return emptyList()
    }
}

private fun extractPreviews(json: JSONObject): MediaPreviews? {
    val original = json.optJSONObject("source")?.let {
        mediaFromPreview(it)
    } ?: return null
    val resolutions = json.optJSONArray("resolutions")?.map {
        mediaFromPreview(getJSONObject(it))
    }.orEmpty()

    val variants = json.optJSONObject("variants")
    return MediaPreviews(
        original,
        resolutions,
        variants?.optJSONObject("gif")?.let(::extractPreviews),
        variants?.optJSONObject("mp4")?.let(::extractPreviews)
    )
}