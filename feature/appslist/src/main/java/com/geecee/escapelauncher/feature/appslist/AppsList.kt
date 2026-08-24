package com.geecee.escapelauncher.feature.appslist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.theme.colours.transparentHalf
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.composables.AnimatedPillSearchBar
import com.geecee.escapelauncher.core.ui.composables.HomeScreenBottomSheet
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.composables.ListGradient
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.composables.TabbedScreen
import com.geecee.escapelauncher.core.ui.composables.TabbedScreenView
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Main App List composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    isBeingShown: Boolean,
    onAppOpened: (app: InstalledApp) -> Unit = {},
    onGoHomeRequest: () -> Unit = {},
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity),
    extraListItems: LazyListScope.(onAppClick: (InstalledApp) -> Unit, onAppLongClick: (InstalledApp) -> Unit) -> Unit = { _, _ -> },
    floatingContent: @Composable BoxScope.(onShowWorkApps: () -> Unit) -> Unit = { _ -> },
    workAppsContent: @Composable BoxScope.(onAppClick: (InstalledApp) -> Unit, onAppLongClick: (InstalledApp) -> Unit) -> Unit = { _, _ -> },
) {
    val haptics = LocalHapticFeedback.current
    val appUsageList by screenTimeViewModel.appUsageUiList.collectAsState()
    val showScreenTimeApp by appsListViewModel.showScreenTimeApp.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_APP)
    val appsListAlignment by appsListViewModel.appsAlignment.collectAsState(initial = DefaultSettingsUi.APPS_ALIGNMENT)
    val hapticFeedbackEnabled by appsListViewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val showSearchBox by appsListViewModel.showSearchBox.collectAsState(initial = DefaultSettings.SHOW_SEARCH_BOX)
    val bottomSearchBox by appsListViewModel.bottomSearch.collectAsState(initial = DefaultSettings.BOTTOM_SEARCH)
    val autoOpenSearch by appsListViewModel.searchAutoOpen.collectAsState(initial = DefaultSettings.SEARCH_AUTO_OPEN)
    val autoOpenAppInSearch by appsListViewModel.automaticallyOpenAppsInSearch.collectAsState(
        initial = DefaultSettings.AUTOMATICALLY_OPEN_APPS_IN_SEARCH
    )
    val apps by appsListViewModel.apps.collectAsState()
    val searchText by appsListViewModel.searchText.collectAsState()
    val searchExpanded by appsListViewModel.searchExpanded.collectAsState()
    val showBottomSheet by appsListViewModel.showBottomSheet.collectAsState()
    val bottomSheetApp by appsListViewModel.bottomSheetApp.collectAsState()
    val showWorkApps by appsListViewModel.showWorkApps.collectAsState()

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

    // This manages tidying everything when the visibility changes
    LaunchedEffect(isBeingShown) {
        if (!isBeingShown) {
            appsListViewModel.onSearchExpandedChanged(false)
            appsListViewModel.setBottomSheetVisible(visibility = false)
            appsListViewModel.setShowWorkApps(show = false)
        } else if (autoOpenSearch) {
            appsListViewModel.onSearchExpandedChanged(true)
        }
    }

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
                .padding(
                    start = if (appsListAlignment == Alignment.Start) 30.dp else 0.dp,
                    top = 0.dp,
                    bottom = 0.dp,
                    end = if (appsListAlignment == Alignment.End) 30.dp else 0.dp
                ),
            horizontalAlignment = appsListAlignment,
        ) {
            // Apps list title
//            item {
//                AppsListHeader(stringResource(R.string.all_apps))
//            }
//
            item {
                Spacer(modifier = Modifier.height(140.dp))
            }

            item {
                TabbedScreenView(
                    screens = listOf(
                        TabbedScreen(
                            title = "All Apps",
                            icon = Icons.AutoMirrored.Filled.List,
                            content = {}),
                        TabbedScreen(
                            title = "Private", icon = Icons.Default.Lock, content = {}),
                        TabbedScreen(
                            title = "Money", icon = Icons.Default.AttachMoney, content = {})
                    ),
                    reverse = appsListAlignment == Alignment.End
                )
            }

            item {
                Spacer(modifier = Modifier.height(15.dp))
            }

            // Search box
            item {
                if (showSearchBox && !bottomSearchBox) {
                    Spacer(modifier = Modifier.height(15.dp))

                    AnimatedPillSearchBar(
                        closedText = stringResource(R.string.search),
                        searchText = searchText,
                        isExpanded = searchExpanded,
                        autoFocus = autoOpenSearch,
                        onExpandedChange = {
                            appsListViewModel.onSearchExpandedChanged(it)
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        },
                        onSearchTextChanged = { query ->
                            appsListViewModel.onSearchTextChanged(query)
                            if (autoOpenAppInSearch && query.length >= 2 && apps.size == 1) {
                                handleAppClick(apps.first())
                            }
                        },
                        onSearchDone = { _, keboardController ->
                            if (apps.isNotEmpty()) {
                                keboardController?.hide()
                                handleAppClick(apps.first())
                            } else {
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            }
                        })

                    Spacer(modifier = Modifier.height(15.dp))
                }
            }

            // Apps
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

            // Extra list items (Secure Folder, Private Space, etc)
            extraListItems(handleAppClick, handleAppLongClick)

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }

            item {
                SettingsSpacer()
            }
        }

        ListGradient() // Adds a gradient to the bottom of the screen just to make it a bit nicer

        // Floating content (Work Apps FAB, etc)
        floatingContent { appsListViewModel.setShowWorkApps(true) }

        // Overlays (Work Apps full screen UI)
        AnimatedVisibility(
            visible = showWorkApps, enter = fadeIn(), exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = { appsListViewModel.setShowWorkApps(show = false) },
                        onLongClick = {},
                        indication = null,
                        interactionSource = null
                    )
                    .background(transparentHalf)
            ) {
                workAppsContent({ app -> // onAppClick
                    appsListViewModel.launchApp(app = app)
                    onGoHomeRequest()
                }, { app -> // onAppLongClick
                    handleAppLongClick(app)
                })
            }
        }

        // Bottom search box
        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .padding(30.dp, 25.dp)
                .fillMaxWidth(), horizontalAlignment = appsListAlignment
        ) {
            if (showSearchBox && bottomSearchBox) {
                Spacer(modifier = Modifier.height(15.dp))

                AnimatedPillSearchBar(
                    closedText = stringResource(R.string.search),
                    searchText = searchText,
                    isExpanded = searchExpanded,
                    autoFocus = autoOpenSearch,
                    onExpandedChange = {
                        appsListViewModel.onSearchExpandedChanged(it)
                        doHapticFeedBack(haptics, hapticFeedbackEnabled)
                    },
                    onSearchTextChanged = { query ->
                        appsListViewModel.onSearchTextChanged(query)
                        if (autoOpenAppInSearch && query.length >= 2 && apps.size == 1) {
                            handleAppClick(apps.first())
                        }
                    },
                    onSearchDone = { _, keboardController ->
                        if (apps.isNotEmpty()) {
                            keboardController?.hide()
                            handleAppClick(apps.first())
                        } else {
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        }
                    })

                SettingsSpacer()
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
    val scrollState = rememberLazyListState()
    Box(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        AppsList(
            scrollState = scrollState,
            isBeingShown = true,
            onAppOpened = {},
            onGoHomeRequest = {})
    }
}
