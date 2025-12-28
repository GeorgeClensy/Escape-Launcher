package com.geecee.escapelauncher.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.utils.InstalledApp

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
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
                                isDisabled = false, // Always enabled for selected and reorderable
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val currentIndex = selectedState.indexOf(item.app)
                                    if (currentIndex > 0) {
                                        val newIndex = currentIndex - 1
                                        val movedItem = selectedState.removeAt(currentIndex)
                                        selectedState.add(newIndex, movedItem)
                                        onAppMoved(currentIndex, newIndex)
                                    }
                                },
                                enabled = selectedState.indexOf(item.app) > 0
                            ) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = "Move Up",
                                    tint = ContentColor
                                )
                            }
                            IconButton(
                                onClick = {
                                    val currentIndex = selectedState.indexOf(item.app)
                                    if (currentIndex < selectedState.size - 1) {
                                        val newIndex = currentIndex + 1
                                        val movedItem = selectedState.removeAt(currentIndex)
                                        selectedState.add(newIndex, movedItem)
                                        onAppMoved(currentIndex, newIndex)
                                    }
                                },
                                enabled = selectedState.indexOf(item.app) < selectedState.size - 1
                            ) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = "Move Down",
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
