package com.looter.rall.ui.postdetail

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.UnstableApi
import com.looter.rall.domain.CommentItem
import com.looter.rall.ui.postdetail.state.CollapsedState
import com.looter.rall.ui.postdetail.state.LoadingMoreState
import com.looter.rall.ui.theme.AppTheme
import com.looter.rall.ui.videoplayer.LocalVideoPlayerController
import com.looter.rall.ui.videoplayer.VideoPlayerController
import dev.jeziellago.compose.markdowntext.MarkdownText

private const val MinRowHeight = 20
private const val DepthReply = 16

@Composable
fun CommentList(
    headerComposable: @Composable () -> Unit,
    comments: List<CommentItem> = emptyList(),
    collapsedState: CollapsedState = CollapsedState(),
    loadingMoreState: LoadingMoreState = LoadingMoreState(),
    onItemClick: (CommentItem) -> Unit = {}
) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
    ) {
        item(key = "header") {
            headerComposable()
        }

        items(comments, key = CommentItem::key, itemContent = { item ->
            val collapsible = CollapsedState.rememberCollapsed(collapsedState, item.key)
            val loadingMore = LoadingMoreState.rememberLoadingMore(loadingMoreState, item.key)

            if (!collapsible.isParentCollapsed) {
                CommentRow(
                    item,
                    collapsible.isCollapsed,
                    loadingMore.isLoading
                ) { onItemClick(item) }
            }
        })
    }
}

@Composable
fun CommentRow(
    item: CommentItem,
    isCollapsed: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit = {}
) {
    Row(Modifier.height(IntrinsicSize.Min)) {
        Depth(item.depth)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp)
                .clickable(onClick = onClick)
        ) {
            when {
                isCollapsed -> Collapsed(item)
                isLoading -> Loading()
                item.isMoreDetails -> LoadMore(item)
                else -> Full(item, onClick)
            }
        }
    }
}


@Composable
private fun Full(item: CommentItem, onClick: () -> Unit) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = item.userName,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    MarkdownText(
        modifier = Modifier.fillMaxWidth(),
        markdown = item.text.trimIndent(),
        style = TextStyle(
            fontSize = 16.sp,
            color = Color.White
        ),
        linkColor = Color.Blue,
        onClick = onClick
    )
}

@Composable
private fun LoadMore(item: CommentItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MinRowHeight.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            modifier = Modifier.size(MinRowHeight.dp),
            imageVector = Icons.Rounded.Add,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            contentDescription = null
        )
        Text(
            text = item.text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun Loading() {
    Row(
        modifier = Modifier
            .height(MinRowHeight.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(MinRowHeight.dp)
                .align(Alignment.CenterVertically),
            strokeWidth = 4.dp
        )
        Text(
            text = "Loading ...",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun Collapsed(item: CommentItem) {
    Row(
        modifier = Modifier
            .height(MinRowHeight.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "u/username",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Icon(
            modifier = Modifier.padding(4.dp, 0.dp),
            imageVector = Icons.Rounded.KeyboardArrowDown,
            tint = MaterialTheme.colorScheme.outlineVariant,
            contentDescription = null
        )
        Text(
            text = item.text, maxLines = 1, overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
private fun Depth(depth: Int) {
    if (depth > 0) {
        for (i in 1..depth) {
            VerticalDivider(
                modifier = Modifier
                    .padding(DepthReply.dp, 0.dp, 0.dp, 0.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@OptIn(UnstableApi::class)
@Preview
@Composable
private fun CommentRowPreview() {
    AppTheme(true) {
        CompositionLocalProvider(
            LocalVideoPlayerController provides VideoPlayerController(LocalContext.current)
        ) {
            Surface {
                Column(
                    modifier = Modifier.height(400.dp)
                ) {
                    CommentRow(
                        CommentItem(
                            id = "1",
                            text = "Politics as usual",
                            depth = 0,
                            isMoreDetails = false
                        ),
                        isCollapsed = false,
                        isLoading = false
                    )
                    CommentRow(
                        CommentItem(
                            id = "2",
                            text = "Load more: 5",
                            depth = 1,
                            isMoreDetails = true
                        ),
                        isCollapsed = false,
                        isLoading = false
                    )
                    CommentRow(
                        CommentItem(
                            id = "3",
                            text = "Need more",
                            depth = 1,
                            isMoreDetails = true
                        ),
                        isCollapsed = false,
                        isLoading = true
                    )
                    CommentRow(
                        CommentItem(
                            id = "4",
                            text = "Collapsed one with text, with some long ass text",
                            depth = 2,
                            isMoreDetails = false
                        ),
                        isCollapsed = true,
                        isLoading = false
                    )
                }
            }
        }
    }
}