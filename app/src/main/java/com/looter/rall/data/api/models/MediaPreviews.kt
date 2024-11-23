package com.looter.rall.data.api.models

import com.looter.rall.domain.RedditMedia
import org.json.JSONObject

data class MediaPreviews(
    val original: RedditMedia,
    val resolutions: List<RedditMedia> = emptyList(),
    val gifPreviews: MediaPreviews? = null,
    val mp4Previews: MediaPreviews? = null,
)

fun extractPreviews(json: JSONObject): MediaPreviews? {
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

private fun mediaFromPreview(json: JSONObject): RedditMedia =
    RedditMedia(json.getString("url"), json.getInt("width"), json.getInt("height"))
