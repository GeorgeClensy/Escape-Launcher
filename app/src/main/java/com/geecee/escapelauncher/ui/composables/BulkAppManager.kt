package com.geecee.escapelauncher.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.geecee.escapelauncher.utils.InstalledApp

@Composable
fun BulkAppManager(
    apps: List<InstalledApp>,
    preSelectedApps: List<InstalledApp> = emptyList(),
    title: String,
    onBackClicked: () -> Unit,
    onAppClicked: (app: InstalledApp, selected: Boolean) -> Unit
) {
    val selectedState = remember { mutableStateListOf<InstalledApp>().apply { addAll(preSelectedApps) } }

    val availableApps = apps.filter { it.packageName != "com.geecee.escapelauncher" }

    // Create a combined list with a spacer marker
    val combinedItems = remember(selectedState.size, availableApps.size) {
        buildList {
            addAll(selectedState.map { ListItem.App(it, isInSelectedSection = true) })
            if (selectedState.isNotEmpty()) {
                add(ListItem.Spacer)
            }
            addAll(availableApps.map { ListItem.App(it, isInSelectedSection = false) })
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsHeader(goBack = { onBackClicked() }, title = title)
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
                    val isSelected = selectedState.contains(item.app)
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
                        isTopOfGroup = if (item.isInSelectedSection) {
                            selectedState.firstOrNull() == item.app
                        } else {
                            availableApps.firstOrNull() == item.app
                        },
                        isBottomOfGroup = if (item.isInSelectedSection) {
                            selectedState.lastOrNull() == item.app
                        } else {
                            availableApps.lastOrNull() == item.app
                        },
                        isDisabled = !item.isInSelectedSection && isSelected,
                        modifier = Modifier.animateItem()
                    )
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