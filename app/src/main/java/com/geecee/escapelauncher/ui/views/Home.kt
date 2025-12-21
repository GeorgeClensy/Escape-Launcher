package com.geecee.escapelauncher.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.composables.Clock
import com.geecee.escapelauncher.ui.composables.Date
import com.geecee.escapelauncher.ui.composables.FirstTimeHelp
import com.geecee.escapelauncher.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.ui.composables.HomeScreenScreenTime
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.WidgetsScreen
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.getHomeAlignment
import com.geecee.escapelauncher.utils.getHomeVAlignment
import com.geecee.escapelauncher.utils.getStringSetting
import com.geecee.escapelauncher.utils.getWidgetHeight
import com.geecee.escapelauncher.utils.getWidgetOffset
import com.geecee.escapelauncher.utils.getWidgetWidth
import com.geecee.escapelauncher.utils.managers.getTotalUsageForDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

/**
 * Parent main home screen composable
 */
 @Composable
fun HomeScreen(
    mainAppModel: MainAppModel, homeScreenModel: HomeScreenModel
) {
    val scrollState = rememberLazyListState()
    val haptics = LocalHapticFeedback.current

    LazyColumn(
        state = scrollState,
        verticalArrangement = getHomeVAlignment(mainAppModel.getContext()),
        horizontalAlignment = getHomeAlignment(mainAppModel.getContext()),
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp, 0.dp)
    ) {
        //Top padding
        item {
            Spacer(Modifier.height(90.dp))
        }

        //Clock
        item {
            if (getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ShowClock), true
                )
            ) {
                Clock(
                    bigClock = getBooleanSetting(
                        context = mainAppModel.getContext(),
                        stringResource(R.string.BigClock),
                        false
                    ),
                    homeAlignment = getHomeAlignment(mainAppModel.getContext()),
                    twelveHour = getBooleanSetting(mainAppModel.getContext(), stringResource(R.string.twelve_hour_clock), false)
                )
            }
        }

        //Date
        item {
            if (getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.show_date), false
                )
            ) {
                Date(
                    homeAlignment = getHomeAlignment(mainAppModel.getContext()),
                    getBooleanSetting(mainAppModel.getContext(), stringResource(R.string.SmallDate), false)
                )
            }
        }

        //Screen time
        item {
            AnimatedVisibility(
                getBooleanSetting(
                    mainAppModel.getContext(), stringResource(R.string.ScreenTimeOnHome), false
                )
            ) {
                val todayUsage = remember { mutableLongStateOf(0L) }
                LaunchedEffect(mainAppModel.shouldReloadScreenTime.value) {
                    withContext(Dispatchers.IO) {
                        val usage = getTotalUsageForDate(mainAppModel.getToday())
                        withContext(Dispatchers.Main) {
                            todayUsage.longValue = usage
                        }
                    }
                }

                HomeScreenScreenTime(formatScreenTime(todayUsage.longValue))
            }
        }

        //Widgets
        item {
            // Find out offset of widget
            var widgetOffset by remember { mutableIntStateOf(0) }
            widgetOffset = if (getStringSetting(
                    mainAppModel.getContext(), "HomeAlignment", "Center"
                ) == "Left"
            ) {
                -8
            } else if (getStringSetting(
                    mainAppModel.getContext(), "HomeAlignment", "Center"
                ) == "Right"
            ) {
                8
            } else {
                0
            }
            widgetOffset += getWidgetOffset(mainAppModel.getContext()).toInt()

            WidgetsScreen(
                context = mainAppModel.getContext(), modifier = Modifier
                    .offset {
                        IntOffset(
                            (widgetOffset.dp)
                                .toPx()
                                .toInt(), 0
                        )
                    }
                    .size(
                        (getWidgetWidth(mainAppModel.getContext())).dp,
                        (getWidgetHeight(mainAppModel.getContext())).dp
                    )
                    .padding(0.dp, (7.5).dp))
        }

        //Apps
        items(homeScreenModel.favoriteApps, key = { app -> app.packageName }) { app ->
            val screenTime =
                remember { mutableLongStateOf(mainAppModel.getCachedScreenTime(app.packageName)) }

            // Update screen time when app changes or shouldReloadScreenTime changes
            LaunchedEffect(app.packageName, mainAppModel.shouldReloadScreenTime.value) {
                val time = mainAppModel.getScreenTimeAsync(app.packageName)
                screenTime.longValue = time
            }

            HomeScreenItem(
                appName = app.displayName,
                screenTime = screenTime.longValue,
                onAppClick = {
                    homeScreenModel.updateSelectedApp(app)

                    AppUtils.openApp(
                        app = app,
                        overrideOpenChallenge = false,
                        openChallengeShow = homeScreenModel.showOpenChallenge,
                        mainAppModel = mainAppModel,
                        homeScreenModel = homeScreenModel
                    )

                    resetHome(homeScreenModel)
                },
                onAppLongClick = {
                    homeScreenModel.showBottomSheet.value = true
                    homeScreenModel.updateSelectedApp(app)
                    doHapticFeedBack(mainAppModel.getContext(), hapticFeedback = haptics)
                },
                showScreenTime = getBooleanSetting(
                    context = mainAppModel.getContext(),
                    setting = stringResource(R.string.ScreenTimeOnApp)
                ),
                modifier = Modifier,
                alignment = getHomeAlignment(mainAppModel.getContext())
            )
        }

        //First time help
        if (getBooleanSetting(
                mainAppModel.getContext(),
                mainAppModel.getContext().resources.getString(R.string.FirstTimeAppDrawHelp),
                true
            )
        ) {
            item {
                Spacer(Modifier.height(15.dp))
            }

            item {
                FirstTimeHelp()
            }
        }

        item {
            Spacer(Modifier.height(90.dp))
        }
    }
}