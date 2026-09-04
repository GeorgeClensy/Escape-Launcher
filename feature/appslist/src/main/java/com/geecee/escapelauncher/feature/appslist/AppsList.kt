package com.geecee.escapelauncher.feature.appslist

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.geecee.escapelauncher.core.ui.composables.TabBar
import com.geecee.escapelauncher.core.ui.composables.TabbedScreen
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Main App List composable
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    scrollState: LazyListState,
    isBeingShown: Boolean,
    tabs: List<TabbedScreen> = emptyList(),
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
    val showSearchBox by appsListViewModel.showSearchBox.collectAsState(initial = DefaultSettings.SHOW_SEARCH_BOX)
    val autoOpenSearch by appsListViewModel.searchAutoOpen.collectAsState(initial = DefaultSettings.SEARCH_AUTO_OPEN)
    val autoOpenAppInSearch by appsListViewModel.automaticallyOpenAppsInSearch.collectAsState(
        initial = DefaultSettings.AUTOMATICALLY_OPEN_APPS_IN_SEARCH
    )
    val apps by appsListViewModel.apps.collectAsState()
    val searchText by appsListViewModel.searchText.collectAsState()
    val searchExpanded by appsListViewModel.searchExpanded.collectAsState()
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

    val selectedTabIndex = remember { mutableIntStateOf(0) }

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

    val heightToTopOfTabs = if(!WindowInsets.isImeVisible) WindowInsets.navigationBars.asPaddingValues()
        .calculateBottomPadding() + 30.dp + 56.dp else 30.dp + 56.dp

    Box(
        modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // The main column with all the items in
        LazyColumn(
            state = scrollState, modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 30.dp,
                    vertical = 0.dp,
                )
                .drawWithContent {
                    drawContent()
                    if (scrollState.canScrollForward || scrollState.canScrollBackward) {
                        val fadeHeight = heightToTopOfTabs.toPx() + 50.dp.toPx()

                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 1f),
                                    Color.Black.copy(alpha = 1f)
                                ), startY = size.height - fadeHeight, endY = size.height
                            ), blendMode = BlendMode.DstOut
                        )
                    }
                }, horizontalAlignment = appsListAlignment, verticalArrangement = Arrangement.Bottom
        ) {
            val isAllApps = selectedTabIndex.intValue == 0

            if (isAllApps) {
                item {
                    val statusBarHeight =
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                    Spacer(
                        modifier = Modifier.height(statusBarHeight + 10.dp)
                    )
                }
            }

            when (selectedTabIndex.intValue) {
                0 -> {
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
                }

                else -> {
                    item {
                        tabs[selectedTabIndex.intValue - 1].content(this)
                    }
                }
            }

            if (isAllApps) {
                item {
                    Spacer(modifier = Modifier.height(heightToTopOfTabs))
                }
            }
        }

        TabBar(
            modifier = Modifier
                .align(
                    when (appsListAlignment) {
                        Alignment.End -> {
                            Alignment.BottomEnd
                        }

                        Alignment.CenterHorizontally -> {
                            Alignment.BottomCenter
                        }

                        else -> {
                            Alignment.BottomStart
                        }
                    }
                )
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                )
                .padding(
                    start = if (appsListAlignment == Alignment.Start || appsListAlignment == Alignment.CenterHorizontally) 30.dp else 0.dp,
                    top = 0.dp,
                    bottom = 30.dp,
                    end = if (appsListAlignment == Alignment.End || appsListAlignment == Alignment.CenterHorizontally) 30.dp else 0.dp
                ),
            screens = listOf(
                TabbedScreen(
                    title = "All Apps", icon = Icons.AutoMirrored.Filled.List
                )
            ) + tabs,
            selectedTabIndex = selectedTabIndex,
            reverse = appsListAlignment == Alignment.End,
            showSearch = showSearchBox,
            searchText = searchText,
            searchExpanded = searchExpanded,
            onSearchExpandedChange = {
                appsListViewModel.onSearchExpandedChanged(it)
                doHapticFeedBack(haptics, hapticFeedbackEnabled)
            },
            onSearchTextChanged = { query ->
                appsListViewModel.onSearchTextChanged(query)
                if (autoOpenAppInSearch && query.length >= 2 && apps.size == 1) {
                    handleAppClick(apps.first())
                }
            },
            onSearchDone = { _, keyboardController ->
                if (apps.isNotEmpty()) {
                    keyboardController?.hide()
                    handleAppClick(apps.first())
                } else {
                    doHapticFeedBack(haptics, hapticFeedbackEnabled)
                }
            })

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
