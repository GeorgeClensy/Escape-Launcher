package com.geecee.escapelauncher.core.ui.composables

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.geecee.escapelauncher.core.theme.ContentColor
import kotlin.math.roundToInt

//Todo: make this not bad and fix the reordering animations
@Composable
fun <T> BulkManager(
    items: List<T>,
    id: (T) -> String,
    label: (T) -> String,
    preSelectedItems: List<T> = emptyList(),
    selectedIdsOverride: Set<String>? = null,
    title: String,
    onBackClicked: () -> Unit,
    onItemClicked: (item: T, selected: Boolean) -> Unit,
    reorderable: Boolean = false,
    hideTitle: Boolean = false,
    hideBack: Boolean = false,
    titleColor: Color = ContentColor,
    topPadding: Boolean = true,
    onItemMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    val selectedState = remember { mutableStateListOf<T>().apply { addAll(preSelectedItems) } }

    // Sync database data when it finishes loading asynchronously
    LaunchedEffect(preSelectedItems) {
        selectedState.clear()
        selectedState.addAll(preSelectedItems)
    }

    val effectiveSelectedIds by remember(selectedState, selectedIdsOverride) {
        derivedStateOf {
            selectedIdsOverride ?: selectedState.map(id).toSet()
        }
    }

    val displayedSelectedItems by remember(items, effectiveSelectedIds, selectedState, reorderable) {
        derivedStateOf {
            if (reorderable) {
                // Keep the order from selectedState if reorderable
                selectedState.filter { effectiveSelectedIds.contains(id(it)) }
            } else {
                // Otherwise use the order from items list but only those that are selected
                items.filter { effectiveSelectedIds.contains(id(it)) }
            }
        }
    }

    val structuralData by remember(items, displayedSelectedItems, effectiveSelectedIds) {
        derivedStateOf {
            val unselected = items.filter { item -> !effectiveSelectedIds.contains(id(item)) }

            val combined = buildList {
                addAll(displayedSelectedItems.map { ListItem.Entry(it, isInSelectedSection = true) })
                if (displayedSelectedItems.isNotEmpty()) {
                    add(ListItem.Spacer)
                }
                addAll(unselected.map { ListItem.Entry(it, isInSelectedSection = false) })
            }

            // Storing boundary ids for quick O(1) lookups inside the rows
            val firstSelectedId = displayedSelectedItems.firstOrNull()?.let(id)
            val lastSelectedId = displayedSelectedItems.lastOrNull()?.let(id)
            val firstUnselectedId = unselected.firstOrNull()?.let(id)
            val lastUnselectedId = unselected.lastOrNull()?.let(id)

            Triple(combined, Pair(firstSelectedId, lastSelectedId), Pair(firstUnselectedId, lastUnselectedId))
        }
    }

    val combinedItems = structuralData.first
    val (firstSelectedId, lastSelectedId) = structuralData.second
    val (firstUnselectedId, lastUnselectedId) = structuralData.third

    var draggedId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var measuredItemHeight by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!hideTitle) {
            item {
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
                    is ListItem.Entry -> "${if (item.isInSelectedSection) "sel" else "avail"}_${id(item.item)}"
                    ListItem.Spacer -> "spacer"
                }
            }
        ) { listItem ->
            when (listItem) {
                is ListItem.Entry -> {
                    val currentItem = listItem.item
                    val currentId = id(currentItem)
                    val currentLabel = label(currentItem)
                    val isSelected = effectiveSelectedIds.contains(currentId)

                    // --- OPTIMIZATION: O(1) checks instead of filtering allocations here ---
                    val isTopOfGroup = if (listItem.isInSelectedSection) {
                        firstSelectedId == currentId
                    } else {
                        firstUnselectedId == currentId
                    }

                    val isBottomOfGroup = if (listItem.isInSelectedSection) {
                        lastSelectedId == currentId
                    } else {
                        lastUnselectedId == currentId
                    }

                    if (listItem.isInSelectedSection && reorderable) {
                        val isDragging = draggedId == currentId
                        val currentIndex = remember(selectedState, currentId) {
                            selectedState.indexOfFirst { id(it) == currentId }
                        }

                        val maxDragUp = -currentIndex * measuredItemHeight.toFloat()
                        val maxDragDown = (selectedState.size - 1 - currentIndex) * measuredItemHeight.toFloat()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged {
                                    if (measuredItemHeight == 0) measuredItemHeight = it.height
                                }
                                .then(if (!isDragging) Modifier.animateItem() else Modifier)
                                .zIndex(if (isDragging) 1f else 0f)
                                .offset {
                                    IntOffset(
                                        0,
                                        if (isDragging) dragOffset.coerceIn(maxDragUp, maxDragDown).roundToInt() else 0
                                    )
                                }
                        ) {
                            SettingsButton(
                                label = currentLabel,
                                onClick = {
                                    selectedState.remove(currentItem)
                                    onItemClicked(currentItem, true)
                                },
                                isTopOfGroup = isTopOfGroup,
                                isBottomOfGroup = isBottomOfGroup,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .pointerInput(currentId) {
                                        detectVerticalDragGestures(
                                            onDragStart = { draggedId = currentId; dragOffset = 0f },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount
                                                val threshold = measuredItemHeight.toFloat() / 2
                                                val fromIndex = selectedState.indexOfFirst { id(it) == currentId }

                                                if (dragOffset > threshold && fromIndex < selectedState.size - 1) {
                                                    val toIndex = fromIndex + 1
                                                    selectedState.add(toIndex, selectedState.removeAt(fromIndex))
                                                    dragOffset -= measuredItemHeight
                                                    onItemMoved(fromIndex, toIndex)
                                                } else if (dragOffset < -threshold && fromIndex > 0) {
                                                    val toIndex = fromIndex - 1
                                                    selectedState.add(toIndex, selectedState.removeAt(fromIndex))
                                                    dragOffset += measuredItemHeight
                                                    onItemMoved(fromIndex, toIndex)
                                                }
                                            },
                                            onDragEnd = { draggedId = null; dragOffset = 0f },
                                            onDragCancel = { draggedId = null; dragOffset = 0f }
                                        )
                                    }
                            ) {
                                Icon(Icons.Default.DragHandle, "Reorder", tint = ContentColor)
                            }
                        }
                    } else {
                        SettingsButton(
                            label = currentLabel,
                            onClick = {
                                if (isSelected) selectedState.remove(currentItem) else selectedState.add(currentItem)
                                onItemClicked(currentItem, isSelected)
                            },
                            isTopOfGroup = isTopOfGroup,
                            isBottomOfGroup = isBottomOfGroup,
                            isDisabled = !listItem.isInSelectedSection && isSelected,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                ListItem.Spacer -> {
                    SettingsSpacer()
                }
            }
        }
    }
}

private sealed class ListItem<out T> {
    data class Entry<T>(val item: T, val isInSelectedSection: Boolean) : ListItem<T>()
    object Spacer : ListItem<Nothing>()
}