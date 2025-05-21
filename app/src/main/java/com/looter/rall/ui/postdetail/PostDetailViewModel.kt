package com.looter.rall.ui.postdetail

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.looter.data.feed.models.CommentItem
import com.looter.data.feed.models.CommentTree
import com.looter.data.feed.models.RedditPost
import com.looter.data.feed.models.findAndReplace
import com.looter.data.feed.models.findNode
import com.looter.data.feed.models.flatMapToItems
import com.looter.data.feed.models.fromJson
import com.looter.rall.ui.post.CURRENT_POST
import com.looter.rall.ui.post.dataStore
import com.looter.rall.ui.postdetail.state.CollapsedState
import com.looter.rall.ui.postdetail.state.LoadingMoreState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: com.looter.data.feed.RedditRepository,
    @ApplicationContext private val context: Context
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
                context.dataStore.data.map { preferences ->
                    preferences[CURRENT_POST]?.let {
                        fromJson(it)
                    }
                }.collect { value ->
                    if (value != null) {
                        _postDetails.value = value
                    }
                    val (details, comments) = repository.getPostDetails(itemId)
                    _postDetails.value = details
                    updateComments(comments)
                }
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