package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * A simplified reorderable selection column that recreates BulkManager functionality.
 */
@Composable
fun <T> ReorderableSelectionLazyColumn(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItems: List<T>,
    id: (T) -> Any,
    label: (T) -> String,
    onItemSelected: (item: T, isSelected: Boolean) -> Unit,
    onItemMoved: (fromIndex: Int, toIndex: Int) -> Unit,
    reorderEnabled: Boolean = false,
    title: String? = null,
    onBackClicked: (() -> Unit)? = null,
    hideBack: Boolean = false,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    topPadding: Boolean = true,
) {
    val lazyListState = rememberLazyListState()
    val localSelectedItems = remember { mutableStateListOf<T>() }

    // Initial and external sync
    LaunchedEffect(selectedItems) {
        val externalIds = selectedItems.map(id)
        val localIds = localSelectedItems.map(id)
        if (externalIds != localIds) {
            localSelectedItems.clear()
            localSelectedItems.addAll(selectedItems)
        }
    }

    val displayData by remember(items) {
        derivedStateOf {
            val selectedIds = localSelectedItems.map(id).toSet()
            val unselected = items.filter { !selectedIds.contains(id(it)) }

            val combined = buildList {
                addAll(localSelectedItems.map { ReorderableSelectionListItem.Entry(it, isSelected = true) })
                if (localSelectedItems.isNotEmpty() && unselected.isNotEmpty()) {
                    add(ReorderableSelectionListItem.Spacer)
                }
                addAll(unselected.map { ReorderableSelectionListItem.Entry(it, isSelected = false) })
            }

            val bounds = BoundaryIds(
                firstSelectedId = localSelectedItems.firstOrNull()?.let(id),
                lastSelectedId = localSelectedItems.lastOrNull()?.let(id),
                firstUnselectedId = unselected.firstOrNull()?.let(id),
                lastUnselectedId = unselected.lastOrNull()?.let(id)
            )

            combined to bounds
        }
    }

    val combinedItems = displayData.first
    val boundaryIds = displayData.second

    val headerCount = if (title != null && onBackClicked != null) 1 else 0
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val adjustedFrom = from.index - headerCount
        val adjustedTo = to.index - headerCount

        // Only allow reordering within the selectedItems section
        if (adjustedFrom >= 0 && adjustedFrom < localSelectedItems.size &&
            adjustedTo >= 0 && adjustedTo < localSelectedItems.size
        ) {
            localSelectedItems.add(adjustedTo, localSelectedItems.removeAt(adjustedFrom))
            onItemMoved(adjustedFrom, adjustedTo)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        if (title != null && onBackClicked != null) {
            item(key = "header") {
                EscapeHeader(
                    goBack = onBackClicked,
                    title = title,
                    hideBack = hideBack,
                    color = titleColor,
                    padding = topPadding
                )
            }
        }

        items(
            items = combinedItems,
            key = { item ->
                when (item) {
                    is ReorderableSelectionListItem.Entry -> "${if (item.isSelected) "sel" else "avail"}_${id(item.item)}"
                    ReorderableSelectionListItem.Spacer -> "spacer"
                }
            }
        ) { listItem ->
            when (listItem) {
                is ReorderableSelectionListItem.Entry -> {
                    val isSelected = listItem.isSelected
                    val item = listItem.item
                    val itemId = id(item)
                    val itemKey = "${if (isSelected) "sel" else "avail"}_$itemId"

                    val isTopOfGroup = if (isSelected) {
                        boundaryIds.firstSelectedId == itemId
                    } else {
                        boundaryIds.firstUnselectedId == itemId
                    }

                    val isBottomOfGroup = if (isSelected) {
                        boundaryIds.lastSelectedId == itemId
                    } else {
                        boundaryIds.lastUnselectedId == itemId
                    }

                    ReorderableItem(reorderableState, key = itemKey) { isDragging ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (!isDragging) Modifier.animateItem() else Modifier)
                        ) {
                            SettingsButton(
                                label = label(item),
                                onClick = {
                                    if (isSelected) {
                                        localSelectedItems.removeAll { id(it) == itemId }
                                    } else {
                                        localSelectedItems.add(item)
                                    }
                                    onItemSelected(item, isSelected)
                                },
                                isTopOfGroup = isTopOfGroup,
                                isBottomOfGroup = isBottomOfGroup,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected && reorderEnabled) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .draggableHandle()
                                ) {
                                    Icon(
                                        Icons.Default.DragHandle,
                                        contentDescription = "Reorder",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                ReorderableSelectionListItem.Spacer -> {
                    SettingsSpacer()
                }
            }
        }
    }
}

private sealed class ReorderableSelectionListItem<out T> {
    data class Entry<T>(val item: T, val isSelected: Boolean) : ReorderableSelectionListItem<T>()
    object Spacer : ReorderableSelectionListItem<Nothing>()
}

private data class BoundaryIds(
    val firstSelectedId: Any?,
    val lastSelectedId: Any?,
    val firstUnselectedId: Any?,
    val lastUnselectedId: Any?
)
