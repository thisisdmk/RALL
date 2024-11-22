package com.looter.rall.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val RedditPostThingType = "t3"

@Serializable
data class RedditPost(
    val title: String = "",
    val subredditName: String = "",
    val numberOfComments: String = "0",
    val upvoteScore: String = "0",
    val type: PostContent = PostContent.Text(""),
    val postId: String = "",
) {
    val redditName: String get() = "${RedditPostThingType}_$postId"
}

fun RedditPost.toJson() = Json.encodeToString(this)
fun fromJson(json: String) = Json.decodeFromString<RedditPost>(json)

@Serializable
sealed class PostContent {

    @Serializable
    data class Image(
        val original: RedditMedia,
        val resolutions: List<RedditMedia> = emptyList()
    ) : PostContent() {
        val allResolutions get() = resolutions + original
    }

    @Serializable
    data class Video(
        val url: String,
        val thumbnailUrls: List<RedditMedia>,
        val asGif: Boolean = false
    ) : PostContent()

    @Serializable
    data class Link(
        val url: String = "",
        val text: String = "",
        val previews: List<RedditMedia> = emptyList()
    ) : PostContent()

    @Serializable
    data class Gallery(
        val items: List<GalleryItem> = emptyList()
    ) : PostContent() {
        val originalUrls: List<String> get() = items.map { it.original.url }
    }

    @Serializable
    data class Text(val text: String = "") : PostContent()
}

@Serializable
data class RedditMedia(
    val url: String, val width: Int, val height: Int
)

@Serializable
data class GalleryItem(
    val original: RedditMedia,
    val previews: List<RedditMedia>,
    val caption: String? = null,
    val outboundUrl: String? = null
) {
    val allResolutions get() = previews + original
}

fun main() {
    val post = RedditPost(
        title = "Cat",
        subredditName = "r/cat",
        numberOfComments = "44",
        upvoteScore = "666",
        type = PostContent.Gallery(
            listOf(
                GalleryItem(
                    RedditMedia("www.reddit.com/url1", 500, 300),
                    listOf(
                        RedditMedia("www.reddit.com/url1", 500, 300),
                        RedditMedia("www.reddit.com/url2", 10, 15)
                    ),
                    "Random cap"
                )
            )
        ),
        postId = "t_post_id",
    )
    println("post: $post")

    val json = post.toJson()
    println("json: $json")
    println("post2: ${fromJson(json)}")
}