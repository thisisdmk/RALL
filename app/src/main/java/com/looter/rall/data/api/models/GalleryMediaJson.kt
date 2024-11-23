package com.looter.rall.data.api.models

import com.looter.rall.domain.RedditMedia
import org.json.JSONArray
import org.json.JSONObject

data class GalleryMediaJson(
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
    val original: RedditMedia? = mediaFromMeta(itemMeta.getJSONObject("s"))
}

fun extractGallery(data: JSONObject): List<GalleryMediaJson> {
    val galleryDataItems = data.optJSONObject("gallery_data")?.optJSONArray("items")
    if (galleryDataItems != null && galleryDataItems.length() > 0) {
        val metadata = data.getJSONObject("media_metadata")
        return galleryDataItems.map { i ->
            val galleryItem = getJSONObject(i)
            val mediaId = galleryItem.getString("media_id")
            val itemMeta = metadata.getJSONObject(mediaId)
            GalleryMediaJson(mediaId, galleryItem, itemMeta)
        }
    }
    return emptyList()
}

private fun mediaFromMeta(json: JSONObject): RedditMedia? {
    val url = json.optString("u")
    if (url.isNullOrBlank()) return null
    return RedditMedia(url, json.getInt("x"), json.getInt("y"))
}

private fun mediaFromMeta(previewsArray: JSONArray): List<RedditMedia> =
    previewsArray.map { mediaFromMeta(getJSONObject(it)) }.filterNotNull()
