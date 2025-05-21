package com.looter.rall.api.models

import com.looter.rall.api.map
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.joinToString
import kotlin.collections.orEmpty
import kotlin.let
import kotlin.text.equals
import kotlin.text.orEmpty
import kotlin.text.replace


data class RedditCommentJson(
    private val data: JSONObject,
    val isMore: Boolean = false
) {
    val id get() = data.getString("id")
    val fullName get() = data.getString("name")
    val author get() = data.getString("author")
    val parentId get() = data.getString("parent_id")
    val isSubmitter get() = data.getBoolean("is_submitter")

    val rawText: String
        get() {
            val body = data.optString("body").orEmpty()
            val meta = mediaMeta
            return if (meta?.isGiphy == true) {
                body.replace(meta.mediaId, meta.url!!)
                    .replace("![gif]", "[gyphy]", true)
            } else {
                body
            }
        }
    val created get() = data.getLong("created_utc")
    val depth get() = data.optInt("depth")
    val score get() = data.getInt("score")
    val hasReply get() = data.get("replies") !is String
    val replies: List<RedditCommentJson> by lazy {
        data.optJSONObject("replies")
            ?.optJSONObject("data")
            ?.optJSONArray("children")
            .let(::toRedditCommentJsons)
    }
    val mediaMeta by lazy { data.optJSONObject("media_metadata")?.let(::CommentMediaMeta) }

    val moreCount get() = data.getInt("count")
    val moreChildren by lazy { data.getJSONArray("children") }
    val moreChildrenIds by lazy { moreChildren.map(JSONArray::getString).joinToString() }

}

data class CommentMediaMeta(
    private val metadata: JSONObject
) {
    val mediaId: String by lazy { metadata.keys().next() }

    private val media by lazy { metadata.getJSONObject(mediaId) }

    private val isImage get() = media.getString("e").equals("Image", true)
    val isGiphy get() = media.optString("t") == "giphy"
    val url: String?
        get() = if (isImage) {
            media.getJSONObject("s").getString("u")
        } else if (isGiphy) {
            media.getString("ext")
        } else {
            media.getJSONObject("s").getString("gif")
        }
}

fun toRedditCommentJsons(commentsArray: JSONArray?): List<RedditCommentJson> = commentsArray?.map {
    val typedJson = getJSONObject(it)
    RedditCommentJson(
        typedJson.getJSONObject("data"),
        typedJson.getString("kind") == "more"
    )
}.orEmpty()