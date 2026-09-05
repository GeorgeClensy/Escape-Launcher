package com.geecee.escapelauncher.feature.appslist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Main App List composable - focuses purely on the list of apps
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(),
    onAppOpened: (app: InstalledApp) -> Unit = {},
    onGoHomeRequest: () -> Unit = {},
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
) {
    val haptics = LocalHapticFeedback.current
    val appUsageList by screenTimeViewModel.appUsageUiList.collectAsState()
    val showScreenTimeApp by appsListViewModel.showScreenTimeApp.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_APP)
    val appsListAlignment by appsListViewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)
    val hapticFeedbackEnabled by appsListViewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val apps by appsListViewModel.apps.collectAsState()
    val showBottomSheet by appsListViewModel.showBottomSheet.collectAsState()
    val bottomSheetApp by appsListViewModel.bottomSheetApp.collectAsState()

    val bottomSheetActions by appsListViewModel.bottomSheetActions.collectAsState()
    val shortcutActions by appsListViewModel.shortcutActions.collectAsState()

    // Standard app interaction logic shared across slots
    val handleAppClick: (InstalledApp) -> Unit = { app ->
        onAppOpened(app)
        appsListViewModel.onSearchExpandedChanged(false)
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    val handleAppLongClick: (InstalledApp) -> Unit = { app ->
        appsListViewModel.setBottomSheetVisible(true)
        appsListViewModel.setBottomSheetApp(app)
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    // Handle UI Events from ViewModel
    LaunchedEffect(Unit) {
        appsListViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AppsListUiEvent.NavigateHome -> onGoHomeRequest()
            }
        }
    }

    val scrollState = rememberLazyListState()

    Box(
        modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // The main column with all the items in
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp),
            horizontalAlignment = appsListAlignment,
            verticalArrangement = Arrangement.Bottom
        ) {
            item {
                val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                Spacer(modifier = Modifier.height(statusBarHeight + 10.dp))
            }

            items(apps, key = { app -> app.packageName }) { app ->
                val screenTime = remember(appUsageList) {
                    screenTimeViewModel.getScreenTime(app.packageName)
                }

                HomeScreenItem(
                    appName = app.displayName,
                    screenTime = formatScreenTime(screenTime),
                    onAppClick = { handleAppClick(app) },
                    onAppLongClick = { handleAppLongClick(app) },
                    showScreenTime = showScreenTimeApp,
                    modifier = Modifier,
                    alignment = appsListAlignment
                )
            }

            item {
                Spacer(modifier = Modifier.height(padding.calculateBottomPadding()))
            }
        }
    }

    // Bottom Sheet
    AnimatedVisibility(showBottomSheet && bottomSheetApp != null) {
        HomeScreenBottomSheet(
            app = bottomSheetApp!!,
            actions = bottomSheetActions,
            onDismissRequest = { appsListViewModel.setBottomSheetVisible(false) },
            shortcutActions = shortcutActions
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppsListPreview() {
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        AppsList(
            onAppOpened = {},
            onGoHomeRequest = {})
    }
}
