package com.looter.rall.domain

private const val RedditPostThingType = "t3"

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

data class GalleryItem(
    val original: RedditMedia,
    val previews: List<RedditMedia>,
    val caption: String? = null,
    val outboundUrl: String? = null
) {
    val allResolutions get() = previews + original
}

sealed class PostContent {
    data class Image(
        val original: RedditMedia,
        val resolutions: List<RedditMedia> = emptyList()
    ) : PostContent() {
        val allResolutions get() = resolutions + original
    }

    data class Video(
        val url: String,
        val thumbnailUrls: List<RedditMedia>,
        val asGif: Boolean = false
    ) : PostContent()

    data class Link(
        val url: String = "",
        val text: String = "",
        val previews: List<RedditMedia> = emptyList()
    ) : PostContent()

    data class Gallery(
        val items: List<GalleryItem> = emptyList()
    ) : PostContent() {
        val originalUrls: List<String> get() = items.map { it.original.url }
    }

    data class Text(val text: String = "") : PostContent()
}