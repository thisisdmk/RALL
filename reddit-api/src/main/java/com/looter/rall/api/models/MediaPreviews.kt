package com.looter.rall.api.models

import com.looter.rall.api.map
import org.json.JSONObject
import kotlin.collections.orEmpty
import kotlin.let

data class MediaPreviews(
    val original: RedditMediaSource,
    val resolutions: List<RedditMediaSource> = emptyList(),
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

private fun mediaFromPreview(json: JSONObject): RedditMediaSource =
    RedditMediaSource(
        json.getString("url"),
        json.getInt("width"),
        json.getInt("height")
    )
