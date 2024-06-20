package com.looter.rall.ui.postdetail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.looter.rall.domain.CommentItem

data class Collapsed(
    val isCollapsed: Boolean = false,
    val isParentCollapsed: Boolean = false
)

@Stable
class CollapsedState {

    private val collapsibleMap = mutableStateMapOf<String, Collapsed>()

    private fun get(commentId: String) = collapsibleMap[commentId] ?: Collapsed()
    fun collapse(comment: CommentItem) = switchTo(comment, collapsed = true)
    fun expand(comment: CommentItem) = switchTo(comment, collapsed = false)
    fun switch(comment: CommentItem, desc: List<CommentItem>) =
        switchTo(comment, desc, !get(comment.key).isCollapsed)

    private fun switchTo(
        comment: CommentItem,
        descendants: List<CommentItem> = emptyList(),
        collapsed: Boolean
    ) {
        collapsibleMap[comment.key] =
            Collapsed(isCollapsed = collapsed, isParentCollapsed = false)
        descendants.forEach {
            updateItem(it.key, collapsed)
        }
    }

    private fun updateItem(
        key: String,
        parentCollapsed: Boolean
    ) {
        val state = collapsibleMap[key]
        collapsibleMap[key] = if (state != null) {
            if (!parentCollapsed) {
                Collapsed(isCollapsed = false, isParentCollapsed = false)
            } else {
                state.copy(isParentCollapsed = true)
            }
        } else {
            Collapsed(isParentCollapsed = parentCollapsed)
        }
    }

    companion object {
        @Composable
        fun rememberCollapsed(state: CollapsedState, commentId: String): Collapsed {
            val collapsed by remember {
                derivedStateOf {
                    state.get(commentId)
                }
            }
            return collapsed
        }
    }
}