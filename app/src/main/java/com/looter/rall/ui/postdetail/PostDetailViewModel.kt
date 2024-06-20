package com.looter.rall.ui.postdetail

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looter.rall.data.RedditRepository
import com.looter.rall.domain.CommentItem
import com.looter.rall.domain.CommentTree
import com.looter.rall.domain.RedditPost
import com.looter.rall.domain.findAndReplace
import com.looter.rall.domain.findNode
import com.looter.rall.domain.flatMapToItems
import com.looter.rall.ui.postdetail.state.CollapsedState
import com.looter.rall.ui.postdetail.state.LoadingMoreState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RedditRepository
) : ViewModel() {

    private val itemId: String = checkNotNull(savedStateHandle["itemId"])

    val collapsedState = CollapsedState()
    val loadingMoreState = LoadingMoreState()

    private val _postDetails = MutableStateFlow(RedditPost())
    val postDetails: StateFlow<RedditPost> = _postDetails.asStateFlow()

    private val _commentList = MutableStateFlow<List<CommentItem>>(listOf())
    val commentList: StateFlow<List<CommentItem>> = _commentList.asStateFlow()

    private val commentTree: MutableState<List<CommentTree>> = mutableStateOf(emptyList())
    private fun updateComments(newComments: List<CommentTree>) {
        commentTree.value = newComments
        _commentList.value = newComments.flatMapToItems()
    }

    init {
        viewModelScope.launch {
            try {
                val (details, comments) = repository.getPostDetails(itemId)
                _postDetails.value = details
                updateComments(comments)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {

    }

    fun commentClicked(comment: CommentItem) {
        if (comment.isMoreDetails) {
            loadMoreComments(comment)
        } else {
            switchCollapsed(comment)
        }
    }

    private fun loadMoreComments(comment: CommentItem) {
        viewModelScope.launch {
            loadingMoreState.set(comment)
            try {
                val moreComments = repository.getMoreComments(
                    comment.parentName,
                    postDetails.value.redditName,
                    comment.moreChildrenIds!!
                )
                val fresh = commentTree.value.toMutableList().apply {
                    findAndReplace(comment, moreComments)
                }
                updateComments(fresh)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            loadingMoreState.unset(comment)
        }
    }

    private fun switchCollapsed(comment: CommentItem) {
        collapsedState.switch(comment, getDescendantsOf(comment))
    }

    private fun getDescendantsOf(comment: CommentItem) = findNode(
        trees = commentTree.value,
        getChildren = CommentTree::children
    ) {
        it.item.id == comment.id
    }?.children.flatMapToItems()
}