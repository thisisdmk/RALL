package com.looter.rall.domain


data class CommentItem(
    val id: String = "",
    val text: String = "",
    val depth: Int = 0,
    val url: String? = null,
    val name: String = "",
    val parentName: String = "",
    val isMoreDetails: Boolean = false,
    val moreChildrenIds: String? = null,
) {
    val userName: String = "u/username"
    val key: String = "${id}_${isMoreDetails}"
}

data class CommentTree(
    val item: CommentItem,
    val children: MutableList<CommentTree> = mutableListOf()
)

fun List<CommentTree>?.flatMapToItems(): List<CommentItem> =
    this?.flatMap { listOf(it.item) + it.children.flatMapToItems() }.orEmpty()

fun MutableList<CommentTree>.findAndReplace(
    old: CommentItem,
    newComments: List<CommentTree>
) {
    val parent = findParent(this, CommentTree::children) { it.item.id == old.id }
    if (parent != null) {
        parent.children.replace(newComments) { it.item.id == old.id }
    } else {
        replace(newComments) { it.item.id == old.id }
    }
}

private fun <T> MutableList<T>.replace(
    replacement: List<T>,
    predicate: (T) -> Boolean
) {
    val index = indexOfFirst(predicate)
    if (index != -1) {
        removeAt(index)
        addAll(index, replacement)
    }
}

fun <T> findNode(
    trees: List<T>,
    getChildren: (T) -> List<T>,
    predicate: (T) -> Boolean
): T? = findNodeWithParent(trees, null, getChildren, predicate)?.first

fun <T> findParent(
    trees: List<T>,
    getChildren: (T) -> List<T>,
    predicate: (T) -> Boolean
): T? = findNodeWithParent(trees, null, getChildren, predicate)?.second

fun <T> findNodeWithParent(
    trees: List<T>,
    parent: T? = null,
    getChildren: (T) -> List<T>,
    predicate: (T) -> Boolean
): Pair<T, T?>? {
    var found: Pair<T, T?>? = null
    for (tree in trees) {
        if (predicate(tree)) {
            found = tree to parent
            break
        } else {
            found = findNodeWithParent(getChildren(tree), tree, getChildren, predicate)
            if (found != null) break
        }
    }
    return found
}
