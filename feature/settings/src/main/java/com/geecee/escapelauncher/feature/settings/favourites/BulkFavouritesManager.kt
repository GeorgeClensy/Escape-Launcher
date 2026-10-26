package com.geecee.escapelauncher.feature.settings.favourites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.ReorderableSelectionLazyColumn
import kotlinx.coroutines.launch

@Composable
fun BulkFavouritesManager(
    favouritesViewModel: FavouritesViewModel = hiltViewModel()
) {
    val installedApps by favouritesViewModel.installedApps.collectAsState()
    val favouriteApps by favouritesViewModel.favoriteApps.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    ReorderableSelectionLazyColumn(
        items = installedApps,
        selectedItems = favouriteApps,
        id = { it.packageName },
        label = { it.displayName },
        title = stringResource(R.string.manage_favourite_apps),
        reorderEnabled = true,
        onItemMoved = { fromIndex, toIndex ->
            val app = favouriteApps[fromIndex]
            coroutineScope.launch {
                favouritesViewModel.modifiedAppsRepository.reorderFavouriteApp(
                    app.packageName, fromIndex, toIndex
                )
            }
        },
        onBackClicked = {
            favouritesViewModel.navRouter.popBackStack()
        },
        onItemSelected = { app, selected ->
            coroutineScope.launch {
                if (selected) {
                    favouritesViewModel.modifiedAppsRepository.removeFavourite(app.packageName)
                } else {
                    favouritesViewModel.modifiedAppsRepository.addFavourite(app.packageName)
                }
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    )
}