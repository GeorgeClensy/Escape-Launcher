package com.geecee.escapelauncher.feature.homescreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.model.AppUsageUiModel
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.Clock
import com.geecee.escapelauncher.core.ui.composables.FirstTimeHelp
import com.geecee.escapelauncher.core.ui.composables.GlanceWidget
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

/**
 * Reflection call to expand the system notification shade from the launcher
 */
private fun expandNotificationsPanel(context: Context) {
    try {
        @SuppressLint("WrongConstant") val service = context.getSystemService("statusbar")

        val statusBarManager = Class.forName("android.app.StatusBarManager")
        val expand = statusBarManager.getMethod("expandNotificationsPanel")
        expand.invoke(service)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    glanceWidgets: @Composable (FlowRowScope.() -> Unit) = {},
    widget: @Composable () -> Unit = {},
    onAppClick: (app: InstalledApp) -> Unit = {},
    onAppLongClick: (app: InstalledApp) -> Unit = {},
    homeViewModel: NewHomeScreenViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val twelveHourClock by homeViewModel.twelveHourClock.collectAsState(initial = DefaultSettings.TWELVE_HOUR_CLOCK)
    val showClock by homeViewModel.showClock.collectAsState(initial = DefaultSettings.SHOW_CLOCK)
    val bigClock by homeViewModel.bigClock.collectAsState(initial = DefaultSettings.BIG_CLOCK)
    val timeParts by homeViewModel.timeParts.collectAsState()
    val showDate by homeViewModel.showDate.collectAsState(initial = DefaultSettings.SHOW_DATE)
    val showScreenTimeHome by homeViewModel.showScreenTimeHome.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_HOME)
    val showScreenTimeApp by homeViewModel.showScreenTimeApp.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_APP)
    val firstTimeHelp by homeViewModel.firstTimeHelp.collectAsState(initial = false)
    val homeAlignment by homeViewModel.homeAlignment.collectAsState(initial = DefaultSettingsUi.HOME_ALIGNMENT)
    val homeVAlignment by homeViewModel.homeVAlignment.collectAsState(initial = DefaultSettingsUi.HOME_V_ALIGNMENT)
    val hapticFeedbackEnabled by homeViewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val screenLayout by homeViewModel.screenLayout.collectAsState(initial = DefaultSettings.SCREEN_LAYOUT)
    val appUsageList by homeViewModel.appUsageUiList.collectAsState()
    val favoriteApps by homeViewModel.favoriteApps.collectAsState()
    val showWallpaper by homeViewModel.showWallpaper.collectAsState(initial = false)
    val totalUsage by homeViewModel.totalUsage.collectAsState()

    // Anything that is not the horizontal layout falls back to the vertical
    // default, so an unknown stored value can never produce a broken page
    val isVerticalLayout = screenLayout != DefaultSettings.SCREEN_LAYOUT_HORIZONTAL

    val (hour, minute, _) = timeParts

    LaunchedEffect(twelveHourClock) {
        homeViewModel.startClockTicker(twelveHourClock)
    }

    Box(modifier.fillMaxSize()) {
        if (isVerticalLayout) {
            // The home page does not scroll: a swipe up belongs to the pager below
            // it (the apps list) and a swipe down is observed for the notification
            // shade. This keeps the apps list one short swipe away no matter how
            // much content the home page has
            Column(
                verticalArrangement = homeVAlignment,
                horizontalAlignment = homeAlignment,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp, 0.dp)
                    .pointerInput(Unit) {
                        // This is for the swipe down to get to quick settings thing.
                        // Observed on the final pass so only movement nothing else
                        // consumed counts: the shade never fires on a drag that
                        // belongs to the pager
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)

                            var totalDrag = 0f
                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Final)
                                if (event.changes.none { it.pressed }) break

                                val drag = event.changes.fold(0f) { total, change ->
                                    total + change.positionChange().y
                                }

                                if (drag > 0f) {
                                    totalDrag += drag

                                    if (totalDrag > 150f) {
                                        expandNotificationsPanel(context)
                                        break
                                    }
                                } else {
                                    totalDrag = 0f
                                }
                            }
                        }
                    }
            ) {
                //Top padding
                Spacer(Modifier.height(90.dp))

                HomeContent(
                    homeViewModel = homeViewModel,
                    hour = hour,
                    minute = minute,
                    showClock = showClock,
                    bigClock = bigClock,
                    showDate = showDate,
                    showScreenTimeHome = showScreenTimeHome,
                    showScreenTimeApp = showScreenTimeApp,
                    firstTimeHelp = firstTimeHelp,
                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                    showWallpaper = showWallpaper,
                    homeAlignment = homeAlignment,
                    favoriteApps = favoriteApps,
                    appUsageList = appUsageList,
                    totalUsage = totalUsage,
                    isVerticalLayout = isVerticalLayout,
                    onAppClick = onAppClick,
                    onAppLongClick = onAppLongClick,
                    glanceWidgets = glanceWidgets,
                    widget = widget
                )

                Spacer(Modifier.height(90.dp))
            }
        } else {
            // The original layout: the home page itself scrolls, so the pager
            // never sees the vertical drags and the shade has to watch the
            // scroll the list cannot consume
            val scrollState = rememberLazyListState()

            // This is for the swipe down to get to quick settings thing
            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    var totalDrag = 0f

                    override fun onPostScroll(
                        consumed: Offset,
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        if (source == NestedScrollSource.UserInput && available.y > 0) {
                            totalDrag += available.y

                            if (totalDrag > 150f) {
                                expandNotificationsPanel(context)
                                totalDrag = 0f
                                return available
                            }
                        } else {
                            totalDrag = 0f
                        }
                        return Offset.Zero
                    }
                }
            }

            LazyColumn(
                state = scrollState,
                verticalArrangement = homeVAlignment,
                horizontalAlignment = homeAlignment,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp, 0.dp)
                    .nestedScroll(nestedScrollConnection)
            ) {
                //Top padding
                item {
                    Spacer(Modifier.height(90.dp))
                }

                item {
                    HomeContent(
                        homeViewModel = homeViewModel,
                        hour = hour,
                        minute = minute,
                        showClock = showClock,
                        bigClock = bigClock,
                        showDate = showDate,
                        showScreenTimeHome = showScreenTimeHome,
                        showScreenTimeApp = showScreenTimeApp,
                        firstTimeHelp = firstTimeHelp,
                        hapticFeedbackEnabled = hapticFeedbackEnabled,
                        showWallpaper = showWallpaper,
                        homeAlignment = homeAlignment,
                        favoriteApps = favoriteApps,
                        appUsageList = appUsageList,
                        totalUsage = totalUsage,
                        isVerticalLayout = isVerticalLayout,
                        onAppClick = onAppClick,
                        onAppLongClick = onAppLongClick,
                        glanceWidgets = glanceWidgets,
                        widget = widget
                    )
                }

                item {
                    Spacer(Modifier.height(90.dp))
                }
            }
        }
    }
}

/**
 * The home page content shared by both screen layouts. The 90dp spacers stay
 * with the callers: the vertical layout keeps them as direct children of its
 * Column, the horizontal layout keeps them as the LazyColumn items around
 * this composable's single item, matching the original page's structure.
 */
@Composable
private fun HomeContent(
    homeViewModel: NewHomeScreenViewModel,
    hour: Int,
    minute: Int,
    showClock: Boolean,
    bigClock: Boolean,
    showDate: Boolean,
    showScreenTimeHome: Boolean,
    showScreenTimeApp: Boolean,
    firstTimeHelp: Boolean,
    hapticFeedbackEnabled: Boolean,
    showWallpaper: Boolean,
    homeAlignment: Alignment.Horizontal,
    favoriteApps: List<InstalledApp>,
    appUsageList: List<AppUsageUiModel>,
    totalUsage: Long,
    isVerticalLayout: Boolean,
    onAppClick: (app: InstalledApp) -> Unit,
    onAppLongClick: (app: InstalledApp) -> Unit,
    glanceWidgets: @Composable (FlowRowScope.() -> Unit),
    widget: @Composable () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current

    //Clock
    if (showClock) {
        Clock(
            hour = hour,
            minute = minute,
            bigClock = bigClock,
            homeAlignment = homeAlignment,
            onClockClick = {
                try {
                    val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("Error", e.message.orEmpty())
                }
            },
            color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary,
            shadow = showWallpaper
        )
    }

    //Glance widgets
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showDate) {
            val dateFormat =
                remember { SimpleDateFormat("EEE d MMM", Locale.getDefault()) }
            var dateText by remember { mutableStateOf(dateFormat.format(Date())) }

            LaunchedEffect(Unit) {
                while (true) {
                    val calendar = Calendar.getInstance()
                    val now = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val delayMillis = calendar.timeInMillis - now
                    delay(delayMillis.milliseconds)
                    dateText = dateFormat.format(Date())
                }
            }

            GlanceWidget(
                text = dateText,
                icon = null,
                iconContentDescription = "",
                homeAlignment = homeAlignment,
                small = true,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_MAIN).apply {
                            addCategory(Intent.CATEGORY_APP_CALENDAR)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        homeViewModel.logException(e)
                    }
                },
                shadow = showWallpaper,
                color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary
            )
        }

        if (showScreenTimeHome) {
            GlanceWidget(
                text = formatScreenTime(totalUsage),
                icon = Icons.Default.Timer,
                iconContentDescription = "Screen Time",
                homeAlignment = homeAlignment,
                small = true,
                onClick = {},
                shadow = showWallpaper,
                color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary
            )
        }

        glanceWidgets()
    }

    Spacer(Modifier.height(10.dp))

    //Widgets
    widget()

    //Apps
    favoriteApps.forEach { app ->
        val screenTime = remember(appUsageList) {
            homeViewModel.getScreenTime(app.packageName)
        }

        HomeScreenItem(
            appName = app.displayName,
            screenTime = formatScreenTime(screenTime),
            onAppClick = {
                onAppClick(app)
                doHapticFeedBack(haptics, hapticFeedbackEnabled)
            },
            onAppLongClick = {
                onAppLongClick(app)
                doHapticFeedBack(hapticFeedback = haptics, enabled = hapticFeedbackEnabled)
            },
            showScreenTime = showScreenTimeApp,
            modifier = Modifier,
            alignment = homeAlignment,
            shadow = showWallpaper,
            color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary,
            screenTimeColor = if (showWallpaper) MaterialTheme.colorScheme.secondaryFixed else MaterialTheme.colorScheme.primary,
            screenTimeAlpha = if (showWallpaper) 1f else 0.5f
        )
    }

    AnimatedVisibility(
        visible = firstTimeHelp,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Column {
            Spacer(Modifier.height(15.dp))

            FirstTimeHelp(
                // The hint follows the layout: swipe up to the apps list in the
                // vertical layout, swipe across to it in the original one
                swipeForAllAppsText = stringResource(
                    if (isVerticalLayout) R.string.swipe_for_all_apps else R.string.swipe_across_for_all_apps
                ),
                holdForSettingsText = stringResource(R.string.hold_for_settings),
                swipeUp = isVerticalLayout
            )
        }
    }
}
