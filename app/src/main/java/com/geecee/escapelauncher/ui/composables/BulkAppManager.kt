package com.geecee.escapelauncher.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.geecee.escapelauncher.core.ui.theme.ContentColor
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlin.math.roundToInt

@Composable
fun BulkAppManager(
    apps: List<InstalledApp>,
    preSelectedApps: List<InstalledApp> = emptyList(),
    title: String,
    onBackClicked: () -> Unit,
    onAppClicked: (app: InstalledApp, selected: Boolean) -> Unit,
    reorderable: Boolean = false,
    hideTitle: Boolean = false,
    hideBack: Boolean = false,
    titleColor: Color = ContentColor,
    topPadding: Boolean = true,
    onAppMoved: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    val selectedState =
        remember { mutableStateListOf<InstalledApp>().apply { addAll(preSelectedApps) } }

    val availableApps = remember(apps) {
        apps.filter { it.packageName != "com.geecee.escapelauncher" }
    }

    // Move this outside the LazyColumn so it's only computed once when selectedState changes
    val selectedPackageNames by remember {
        derivedStateOf { selectedState.map { it.packageName }.toSet() }
    }

    // Drag state for reorderable items
    var draggedPackageName by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var measuredItemHeight by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()

    // Create a combined list with a spacer marker
    val combinedItems by remember(apps) {
        derivedStateOf {
            buildList {
                addAll(selectedState.map { ListItem.App(it, isInSelectedSection = true) })
                if (selectedState.isNotEmpty()) {
                    add(ListItem.Spacer)
                }
                addAll(availableApps.map { ListItem.App(it, isInSelectedSection = false) })
            }
        }
    }

    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        if (!hideTitle) {
            item {
                SettingsHeader(goBack = { onBackClicked() }, title = title, hideBack = hideBack, color = titleColor, padding = topPadding)
            }
        }

        items(
            items = combinedItems,
            key = { item ->
                when (item) {
                    is ListItem.App -> "${if (item.isInSelectedSection) "selected" else "available"}_${item.app.packageName}"
                    ListItem.Spacer -> "spacer"
                }
            }
        ) { item ->
            when (item) {
                is ListItem.App -> {
                    val isSelected = selectedPackageNames.contains(item.app.packageName)
                    
                    val isTopOfGroup = if (item.isInSelectedSection) {
                        selectedState.firstOrNull() == item.app
                    } else {
                        availableApps.firstOrNull() == item.app
                    }
                    val isBottomOfGroup = if (item.isInSelectedSection) {
                        selectedState.lastOrNull() == item.app
                    } else {
                        availableApps.lastOrNull() == item.app
                    }

                    if (item.isInSelectedSection && reorderable) {
                        val isDragging = draggedPackageName == item.app.packageName
                        
                        // Calculate drag limits
                        val currentIndex = selectedState.indexOf(item.app)
                        val maxDragUp = -currentIndex * measuredItemHeight.toFloat()
                        val maxDragDown = (selectedState.size - 1 - currentIndex) * measuredItemHeight.toFloat()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onSizeChanged { size ->
                                    if (measuredItemHeight == 0) {
                                        measuredItemHeight = size.height
                                    }
                                }
                                .then(if (!isDragging) Modifier.animateItem() else Modifier)
                                .zIndex(if (isDragging) 1f else 0f)
                                .offset {
                                    IntOffset(
                                        x = 0,
                                        y = if (isDragging) {
                                            dragOffset.coerceIn(maxDragUp, maxDragDown).roundToInt()
                                        } else 0
                                    )
                                }
                        ) {
                            SettingsButton(
                                label = item.app.displayName,
                                onClick = {
                                    if (isSelected) {
                                        selectedState.remove(item.app)
                                    } else {
                                        selectedState.add(item.app)
                                    }
                                    onAppClicked(item.app, isSelected)
                                },
                                isTopOfGroup = isTopOfGroup,
                                isBottomOfGroup = isBottomOfGroup,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .pointerInput(item.app.packageName) {
                                        detectVerticalDragGestures(
                                            onDragStart = {
                                                draggedPackageName = item.app.packageName
                                                dragOffset = 0f
                                            },
                                            onVerticalDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffset += dragAmount

                                                val itemHeight = measuredItemHeight.toFloat()
                                                val threshold = itemHeight / 2

                                                val currentPkg = draggedPackageName ?: return@detectVerticalDragGestures
                                                val fromIndex = selectedState.indexOfFirst { it.packageName == currentPkg }
                                                if (fromIndex == -1) return@detectVerticalDragGestures

                                                if (dragOffset > threshold && fromIndex < selectedState.size - 1) {
                                                    // Move item down
                                                    val toIndex = fromIndex + 1
                                                    val movedItem = selectedState.removeAt(fromIndex)
                                                    selectedState.add(toIndex, movedItem)
                                                    dragOffset -= itemHeight
                                                    onAppMoved(fromIndex, toIndex)
                                                } else if (dragOffset < -threshold && fromIndex > 0) {
                                                    // Move item up
                                                    val toIndex = fromIndex - 1
                                                    val movedItem = selectedState.removeAt(fromIndex)
                                                    selectedState.add(toIndex, movedItem)
                                                    dragOffset += itemHeight
                                                    onAppMoved(fromIndex, toIndex)
                                                }
                                            },
                                            onDragEnd = {
                                                draggedPackageName = null
                                                dragOffset = 0f
                                            },
                                            onDragCancel = {
                                                draggedPackageName = null
                                                dragOffset = 0f
                                            }
                                        )
                                    }
                            ) {
                                Icon(
                                    Icons.Default.DragHandle,
                                    contentDescription = "Drag to reorder",
                                    tint = ContentColor
                                )
                            }
                        }
                    } else {
                        SettingsButton(
                            label = item.app.displayName,
                            onClick = {
                                if (isSelected) {
                                    selectedState.remove(item.app)
                                } else {
                                    selectedState.add(item.app)
                                }
                                onAppClicked(item.app, isSelected)
                            },
                            isTopOfGroup = isTopOfGroup,
                            isBottomOfGroup = isBottomOfGroup,
                            isDisabled = !item.isInSelectedSection && isSelected,
                            modifier = Modifier.animateItem()
                        )
                    }
                }

                ListItem.Spacer -> AnimatedVisibility(
                    visible = selectedState.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SettingsSpacer()
                }
            }
        }

        item {
            SettingsSpacer()
        }

        item {
            SettingsSpacer()
        }
    }
}

private sealed class ListItem {
    data class App(val app: InstalledApp, val isInSelectedSection: Boolean) : ListItem()
    object Spacer : ListItem()
}
