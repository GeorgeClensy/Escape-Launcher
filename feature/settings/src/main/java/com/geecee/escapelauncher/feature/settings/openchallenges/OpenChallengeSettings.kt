package com.geecee.escapelauncher.feature.settings.openchallenges

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
fun OpenChallengeSettings(
    openChallengeViewModel: OpenChallengeViewModel = hiltViewModel()
) {
    val installedApps by openChallengeViewModel.installedApps.collectAsState()
    val openChallengeAppIds by openChallengeViewModel.challengeAppIds.collectAsState()

    ReorderableSelectionLazyColumn(
        items = installedApps,
        selectedItems = installedApps.filter { item -> openChallengeAppIds.any { it.packageName == item.packageName } },
        id = { it.packageName },
        label = { it.displayName },
        title = stringResource(com.geecee.escapelauncher.core.ui.R.string.manage_open_challenges),
        onBackClicked = {
            openChallengeViewModel.navRouter.popBackStack()
        },
        onItemMoved = { _, _ -> },
        onItemSelected = { app, selected ->
            if (selected) {
                openChallengeViewModel.removeChallengeFromApp(app.packageName)
            } else {
                openChallengeViewModel.addChallengeToApp(app.packageName)
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    )
}