package com.looter.rall.ui.post

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.looter.rall.R
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun LinkWithPreview(
    thumbnailUrl: String,
    link: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    onClick: () -> Unit = {}
) {
    Column(modifier) {
        Box(
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            ClickableImage(thumbnailUrl) { onClick() }
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f))
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    modifier = Modifier
                        .weight(0.1f),
                    text = link,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    modifier = Modifier
                        .size(24.dp),
                    painter = painterResource(R.drawable.icon_open_in_browser),
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }
        if (!text.isNullOrBlank()) {
            MarkdownText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp),
                markdown = text.trimIndent(),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                linkColor = Color.Blue,
                onClick = onClick
            )
        }
    }
}