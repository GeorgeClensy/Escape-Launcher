package com.geecee.escapelauncher.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.SoftwareKeyboardController
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.composables.TabDisplay
import com.geecee.escapelauncher.core.ui.composables.TabbedScreen
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.appslist.AppsList
import com.geecee.escapelauncher.feature.appslist.AppsListViewModel

@Composable
fun AppsListTabScreen(
    appsListViewModel: AppsListViewModel,
    appsListTabs: List<TabbedScreen?>,
    handleAppClick: (InstalledApp) -> Unit,
    handleAppLongClick: (InstalledApp) -> Unit,
    hapticFeedbackEnabled: Boolean
) {
    val haptics = LocalHapticFeedback.current

    val searchText by appsListViewModel.searchText.collectAsState()
    val searchExpanded by appsListViewModel.searchExpanded.collectAsState()
    val showSearchBox by appsListViewModel.showSearchBox.collectAsState(initial = DefaultSettings.SHOW_SEARCH_BOX)
    val appsListAlignment by appsListViewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)
    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val apps by appsListViewModel.apps.collectAsState()
    val autoOpenAppInSearch by appsListViewModel.automaticallyOpenAppsInSearch.collectAsState(
        initial = DefaultSettings.AUTOMATICALLY_OPEN_APPS_IN_SEARCH
    )

    TabDisplay(
        screens = listOf(
            TabbedScreen(
                title = "All Apps", icon = Icons.Rounded.Apps, content = { padding ->
                    AppsList(
                        padding = padding,
                        onAppOpened = { app ->
                            handleAppClick(app)
                        },
                        onAppLongClick = { app ->
                            handleAppLongClick(app)
                        },
                        appsListViewModel = appsListViewModel
                    )
                })
        ) + appsListTabs.filterNotNull(),
        selectedTabIndex = selectedTabIndex,
        alignment = appsListAlignment,
        showSearch = showSearchBox,
        searchText = searchText,
        searchExpanded = searchExpanded,
        onSearchExpandedChange = {
            appsListViewModel.onSearchExpandedChanged(it)
            doHapticFeedBack(haptics, hapticFeedbackEnabled)
        },
        onSearchTextChanged = { query: String ->
            appsListViewModel.onSearchTextChanged(query)
            if (autoOpenAppInSearch && query.length >= 2 && apps.size == 1) {
                handleAppClick(apps.first())
            }
        },
        onSearchDone = { _: String, keyboardController: SoftwareKeyboardController? ->
            if (apps.isNotEmpty()) {
                keyboardController?.hide()
                handleAppClick(apps.first())
            } else {
                doHapticFeedBack(haptics, hapticFeedbackEnabled)
            }
        })
}
