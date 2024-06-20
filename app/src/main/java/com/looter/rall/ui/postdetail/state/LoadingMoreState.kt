package com.looter.rall.ui.postdetail.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.looter.rall.domain.CommentItem

data class LoadingMore(
    val isLoading: Boolean = false
)

@Stable
class LoadingMoreState {

    private val map = mutableStateMapOf<String, LoadingMore>()

    private fun get(commentId: String) = map[commentId] ?: LoadingMore()
    fun set(comment: CommentItem) = switchTo(comment, true)
    fun unset(comment: CommentItem) = switchTo(comment, false)

    private fun switchTo(comment: CommentItem, loading: Boolean) {
        map[comment.key] = LoadingMore(loading)
    }

    companion object {
        @Composable
        fun rememberLoadingMore(state: LoadingMoreState, commentId: String): LoadingMore {
            val loading by remember {
                derivedStateOf {
                    state.get(commentId)
                }
            }
            return loading
        }
    }
}