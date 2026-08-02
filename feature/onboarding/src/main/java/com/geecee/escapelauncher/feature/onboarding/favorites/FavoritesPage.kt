package com.geecee.escapelauncher.feature.onboarding.favorites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.theme.EscapeThemePreview
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.BulkManager
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesPage(
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val installedApps by viewModel.installedApps.collectAsState()
    val favoriteApps by viewModel.favoriteApps.collectAsState()

    // Add a small delay before rendering the full list to prevent jank during page transition
    var showList by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        showList = true
    }

    Box(
        modifier
            .fillMaxSize()
            .padding(start = 30.dp, end = 30.dp)
    ) {
        if (showList) {
            BulkManager(
                items = installedApps,
                id = { it.packageName },
                label = { it.displayName },
                preSelectedItems = favoriteApps,
                title = stringResource(R.string.choose_your_favourite_apps),
                reorderable = true,
                onItemMoved = { fromIndex, toIndex ->
                    val app = favoriteApps[fromIndex]
                    viewModel.reorderFavorite(app.packageName, fromIndex, toIndex)
                },
                onBackClicked = { },
                hideTitle = false,
                hideBack = true,
                topPadding = false,
                titleColor = MaterialTheme.colorScheme.primary,
                onItemClicked = { app, selected ->
                    if (selected) {
                        viewModel.removeFavorite(app.packageName)
                    } else {
                        viewModel.addFavorite(app.packageName)
                    }
                })
        }
    }
}

@Preview
@Composable
fun PrevFavouritesPage() {
    EscapeThemePreview {
        Box(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
        ) {
            FavoritesPage()
        }
    }
}