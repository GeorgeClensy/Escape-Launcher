package com.geecee.escapelauncher.feature.settings.openchallenges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.ReorderableSelectionLazyColumn

@Composable
fun OpenChallengeSettings(
    modifier: Modifier = Modifier,
    openChallengeViewModel: OpenChallengeViewModel = hiltViewModel()
) {
    val countdownSeconds by openChallengeViewModel.countdownSeconds.collectAsState()
    val installedApps by openChallengeViewModel.installedApps.collectAsState()
    val openChallengeAppIds by openChallengeViewModel.challengeAppIds.collectAsState()

    OpenChallengeSettingsContent(
        installedApps = installedApps,
        challengeApps = openChallengeAppIds,
        countdownSeconds = countdownSeconds,
        modifier = modifier,
        onBack = openChallengeViewModel.navRouter::popBackStack,
        onDurationChanged = openChallengeViewModel::setCountdownSeconds,
        onAppSelected = { app, isSelected ->
            if (isSelected) {
                openChallengeViewModel.removeChallengeFromApp(app.packageName)
            } else {
                openChallengeViewModel.addChallengeToApp(app.packageName)
            }
        }
    )
}

@Composable
private fun OpenChallengeSettingsContent(
    installedApps: List<InstalledApp>,
    challengeApps: List<InstalledApp>,
    countdownSeconds: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onDurationChanged: (Int) -> Unit,
    onAppSelected: (InstalledApp, Boolean) -> Unit
) {
    ReorderableSelectionLazyColumn(
        items = installedApps,
        selectedItems = installedApps.filter { app ->
            challengeApps.any { it.packageName == app.packageName }
        },
        id = { it.packageName },
        label = { it.displayName },
        title = stringResource(R.string.manage_open_challenges),
        onBackClicked = onBack,
        headerContent = {
            CountdownDurationSetting(
                seconds = countdownSeconds,
                onDurationChanged = onDurationChanged
            )
        },
        selectedSectionTitle = stringResource(R.string.countdown_apps_heading),
        unselectedSectionTitle = stringResource(R.string.countdown_other_apps_heading),
        onItemMoved = { _, _ -> },
        onItemSelected = onAppSelected,
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun OpenChallengeSettingsPreview() {
    MaterialTheme {
        OpenChallengeSettingsContent(
            installedApps = emptyList(),
            challengeApps = emptyList(),
            countdownSeconds = DefaultSettings.COUNTDOWN_SECONDS,
            onBack = {},
            onDurationChanged = {},
            onAppSelected = { _, _ -> }
        )
    }
}
