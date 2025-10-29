package com.geecee.escapelauncher.ui.composables

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
    preSelectedApps: List<InstalledApp> = emptyList<InstalledApp>(),
    title: String,
    onBackClicked: () -> Unit,
    onAppClicked: (app: InstalledApp, selected: Boolean) -> Unit
) {
    val selectedState = remember { mutableStateListOf<InstalledApp>().apply { addAll(preSelectedApps) } }

    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SettingsHeader(goBack = { onBackClicked() }, title = title)
        }

        items(items = apps) { app ->
            val isSelected = selectedState.contains(app)
            SettingsButton(
                label = app.displayName,
                onClick = {
                    if (isSelected) {
                        selectedState.remove(app)
                    } else {
                        selectedState.add(app)
                    }

                    onAppClicked(app, isSelected)
                },
                isTopOfGroup = apps.firstOrNull() == app,
                isBottomOfGroup = apps.lastOrNull() == app,
                isDisabled = isSelected
            )
        }

        item {
            SettingsSpacer()
        }
    }
}