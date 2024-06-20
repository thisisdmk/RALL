package com.looter.rall.ui

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems

inline fun <T : Any> LazyListScope.lazyItemsIndexed(
    items: LazyPagingItems<T>,
    noinline key: ((index: Int, item: T) -> Any)? = null,
    crossinline contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
    crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T) -> Unit
) = items(
    count = items.itemCount,
    key = if (key != null) { index -> key(index, items[index]!!) } else null,
    contentType = { index -> contentType(index, items[index]!!) }
) {
    itemContent(it, items[it]!!)
}