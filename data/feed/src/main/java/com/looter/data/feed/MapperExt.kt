package com.looter.data.feed

import com.looter.data.feed.models.GalleryItem
import com.looter.data.feed.models.PostContent
import com.looter.data.feed.models.PostContent.Gallery
import com.looter.data.feed.models.PostContent.Image
import com.looter.data.feed.models.PostContent.Link
import com.looter.data.feed.models.PostContent.Text
import com.looter.data.feed.models.PostContent.Video
import com.looter.data.feed.models.RedditMedia
import com.looter.data.feed.models.RedditPost
import com.looter.rall.api.models.RedditMediaSource
import com.looter.rall.api.models.RedditPostJson


fun toRedditPost(json: RedditPostJson): RedditPost? {
    val type = getContent(json) ?: return null
    return RedditPost(
        json.title.trim(),
        json.subredditName,
        json.author,
        json.numberOfComments,
        json.upvoteScore,
        type,
        json.id
    )
}

private fun getContent(post: RedditPostJson): PostContent? {
    return if (post.isVideo) {
        videoContent(post)
    } else if (post.isGallery) {
        galleryContent(post)
    } else if (post.isGif) {
        gifContent(post)
    } else if (post.isImage) {
        imageContent(post)
    } else if (post.isText) {
        Text(post.text)
    } else {
        linkContent(post)
    }
}

private fun videoContent(post: RedditPostJson): Video? {
    val dashUrl = post.dashUrl
    if (dashUrl.isNullOrBlank()) return null
    return Video(
        url = dashUrl,
        thumbnailUrls = post.previews?.resolutions.toMedias(),
        asGif = false
    )
}

private fun galleryContent(post: RedditPostJson): Gallery? {
    val items = post.galleryData.mapNotNull {
        if (it.original != null) {
            GalleryItem(
                original = it.original!!.toMedia(),
                previews = it.previews.toMedias(),
                caption = it.caption,
                outboundUrl = it.outboundUrl
            )
        } else null
    }
    return if (items.isEmpty()) null else Gallery(items)
}

private fun gifContent(post: RedditPostJson): Video? {
    val gifUrl = post.gifUrl
    if (gifUrl.isNullOrBlank()) return null
    return Video(
        url = gifUrl,
        thumbnailUrls = post.previews?.resolutions.toMedias(),
        asGif = true
    )
}

private fun imageContent(post: RedditPostJson): Image? {
    val original = post.previews?.original ?: return null
    return Image(original.toMedia(), post.previews?.resolutions.toMedias())
}

private fun linkContent(post: RedditPostJson): Link? {
    val resolutions = post.previews?.resolutions.toMedias()
    if (resolutions.isEmpty()) return null
    val orig = post.previews?.original?.toMedia()
    val previews = if (orig != null) resolutions + orig else resolutions
    return Link(post.url, post.text, previews)
}

private fun RedditMediaSource.toMedia() = RedditMedia(url, width, height)
private fun List<RedditMediaSource>?.toMedias() = this?.mapNotNull { it.toMedia() }.orEmpty()