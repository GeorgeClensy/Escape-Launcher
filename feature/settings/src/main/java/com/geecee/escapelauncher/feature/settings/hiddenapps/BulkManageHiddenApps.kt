package com.geecee.escapelauncher.feature.settings.hiddenapps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.composables.ReorderableSelectionLazyColumn

@Composable
fun BulkManageHiddenApps(
    hiddenAppsViewModel: HiddenAppsViewModel = hiltViewModel()
) {
    val hiddenPackageIds by hiddenAppsViewModel.hiddenPackageIds.collectAsState()
    val installedApps by hiddenAppsViewModel.installedApps.collectAsState()

    ReorderableSelectionLazyColumn(
        items = installedApps,
        selectedItems = installedApps.filter { item ->
            hiddenPackageIds.contains(
                item.packageName
            )
        },
        id = { it.packageName },
        label = { it.displayName },
        title = stringResource(com.geecee.escapelauncher.core.ui.R.string.manage_hidden_apps),
        onBackClicked = {
            hiddenAppsViewModel.navRouter.popBackStack()
        },
        onItemMoved = { _, _ -> },
        onItemSelected = { app, selected ->
            if (selected) {
                hiddenAppsViewModel.unhideApp(app.packageName)
            } else {
                hiddenAppsViewModel.hideApp(app.packageName)
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    )
}