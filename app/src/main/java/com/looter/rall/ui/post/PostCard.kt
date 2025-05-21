@file:kotlin.OptIn(ExperimentalMaterial3AdaptiveApi::class)

package com.looter.rall.ui.post

import androidx.annotation.OptIn
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.looter.data.feed.models.PostContent
import com.looter.data.feed.models.PostContent.Gallery
import com.looter.data.feed.models.PostContent.Image
import com.looter.data.feed.models.PostContent.Link
import com.looter.data.feed.models.PostContent.Text
import com.looter.data.feed.models.PostContent.Video
import com.looter.data.feed.models.RedditMedia
import com.looter.data.feed.models.RedditPost
import com.looter.rall.R
import com.looter.rall.ui.bottomBorder
import com.looter.rall.ui.theme.AppTheme
import com.looter.rall.ui.topBorder
import com.looter.rall.videoplayer.LocalVideoPlayerController
import com.looter.rall.videoplayer.VideoPlayerController
import com.looter.rall.videoplayer.VideoPlayerWithThumbnail

@Composable
fun PostCardLayout(
    post: RedditPost,
    imageContent: @Composable (Image) -> Unit = { NotImplemented(it) },
    videoContent: @Composable (Video) -> Unit = { NotImplemented(it) },
    galleryContent: @Composable (Gallery) -> Unit = { NotImplemented(it) },
    linkContent: @Composable (Link) -> Unit = { NotImplemented(it) },
    textContent: @Composable (Text) -> Unit = { NotImplemented(it) },
    controller: PostCardController = PostCardController.PostCardControllerNoop
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        TextButton(
            modifier = Modifier
                .padding(12.dp, 12.dp, 16.dp, 0.dp)
                .height(24.dp),
            onClick = { controller.navigateToSubreddit(post) },
            contentPadding = PaddingValues(4.dp),
            shape = RectangleShape
        ) {
            Icon(
                modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 0.dp),
                painter = painterResource(R.drawable.icon_communities),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null
            )
            Text(
                text = "${post.subredditName} (${post.type.javaClass.simpleName})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            modifier = Modifier
                .padding(16.dp, 8.dp, 16.dp, 16.dp)
                .fillMaxWidth()
                .clickable(onClick = { controller.navigateToPost(post) }),
            text = post.title,
            style = MaterialTheme.typography.titleLarge
        )
        when (val type = post.type) {
            is Image -> imageContent(type)
            is Video -> videoContent(type)
            is Gallery -> galleryContent(type)
            is Link -> linkContent(type)
            is Text -> textContent(type)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 12.dp, 16.dp, 12.dp)
                .height(48.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    modifier = Modifier
                        .padding(0.dp, 0.dp, 12.dp, 0.dp)
                        .height(16.dp),
                    painter = painterResource(R.drawable.icon_upvote),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null
                )
                Text(
                    text = post.upvoteScore,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                )
            }
            TextButton(
                onClick = { controller.navigateToPost(post) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 0.dp, 0.dp, 0.dp)
                    .border(
                        1.dp.div(2),
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    )
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = "${post.numberOfComments} comments",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun NotImplemented(type: PostContent? = null) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "Not implemented: ${type?.javaClass?.simpleName ?: "nothing"}",
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@kotlin.OptIn(ExperimentalMaterial3AdaptiveApi::class)
@OptIn(UnstableApi::class)
@Composable
fun PostCard(
    item: RedditPost,
    screenKey: String,
    controller: PostCardController = PostCardController.PostCardControllerNoop,
    playerState: State<VideoPlayerController.PlayerState> = LocalVideoPlayerController.current.rememberPlayerState(),
    windowSize: IntSize = currentWindowSize()
) {
    PostCardLayout(
        post = item,
        controller = controller,
        imageContent = { image ->
            val preview =
                image.allResolutions.pickWidthGreaterThan(windowSize.width)
            val heightDp = calculateHeightDp(windowSize.width, preview.width, preview.height)
            ClickableImage(
                imageUrl = preview.url,
                modifier = Modifier
                    .height(heightDp)
                    .topBorder(MaterialTheme.colorScheme.outlineVariant, 1.0f)
                    .bottomBorder(MaterialTheme.colorScheme.outlineVariant, 1.0f)
            ) { controller.navigateToImage(item) }
        },
        videoContent = { video ->
            val thumbnail = video.thumbnailUrls.pickWidthGreaterThan(windowSize.width)
            val heightDp = calculateHeightDp(windowSize.width, thumbnail.width, thumbnail.height)
            VideoPlayerWithThumbnail(
                url = video.url,
                thumbnailUrl = thumbnail.url,
                mediaKey = item.redditName,
                modifier = Modifier.height(heightDp),
                playerState = playerState,
                screenKey = screenKey,
                isGifPlayer = video.asGif
            ) { controller.navigateToVideo(item) }
        },
        galleryContent = { gallery ->
            val previews = gallery.items.map {
                it.allResolutions.pickWidthGreaterThan(windowSize.width)
            }
            val first = previews.first()
            val heightDp = calculateHeightDp(windowSize.width, first.width, first.height)
            ImageGallery(
                urls = previews.map { it.url },
                modifier = Modifier.height(heightDp)
            ) { _, _ -> controller.navigateToGallery(gallery.originalUrls) }
        },
        linkContent = { link ->
            val preview = link.previews.pickWidthGreaterThan(windowSize.width)
            val heightDp = calculateHeightDp(windowSize.width, preview.width, preview.height)
            LinkWithPreview(
                thumbnailUrl = preview.url,
                link = link.url,
                modifier = Modifier.height(heightDp),
                text = link.text
            ) { controller.openLink(link.url) }
        },
        textContent = { textContent ->
            TextPostContent(textContent.text) { controller.navigateToPost(item) }
        }
    )
}

private fun List<RedditMedia>.pickWidthGreaterThan(width: Int) =
    sortedBy(RedditMedia::width).find { it.width >= width } ?: last()

@Composable
private fun calculateHeightDp(windowWidthPx: Int, contentWidthPx: Int, contentHeightPx: Int): Dp =
    calculateContentHeight(windowWidthPx, contentWidthPx, contentHeightPx).let { px ->
        with(LocalDensity.current) { px.toDp() }
    }

fun calculateContentHeight(windowWidth: Int, contentWidth: Int, contentHeight: Int): Float {
    return windowWidth.toFloat() / aspectRatio(contentWidth, contentHeight)
}

private fun aspectRatio(width: Int, height: Int): Float {
    if (height == 0 || width == 0) return 1f
    return width.toFloat() / height.toFloat()
}

@OptIn(UnstableApi::class)
@Preview
@Composable
private fun PostCardLayoutPreview() {
    AppTheme(true) {
        CompositionLocalProvider(
            LocalVideoPlayerController provides VideoPlayerController(LocalContext.current)
        ) {
            Surface {
                PostCardLayout(
                    RedditPost()
                )
            }
        }
    }
}