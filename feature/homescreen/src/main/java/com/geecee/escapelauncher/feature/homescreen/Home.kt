package com.geecee.escapelauncher.feature.homescreen

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.DefaultSettingsUi
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.Clock
import com.geecee.escapelauncher.core.ui.composables.FirstTimeHelp
import com.geecee.escapelauncher.core.ui.composables.GlanceWidget
import com.geecee.escapelauncher.core.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.feature.weather.WeatherViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
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
    val haptics = LocalHapticFeedback.current

    val twelveHourClock by homeViewModel.twelveHourClock.collectAsState(initial = DefaultSettings.TWELVE_HOUR_CLOCK)
    val showClock by homeViewModel.showClock.collectAsState(initial = DefaultSettings.SHOW_CLOCK)
    val bigClock by homeViewModel.bigClock.collectAsState(initial = DefaultSettings.BIG_CLOCK)
    val timeParts by homeViewModel.timeParts.collectAsState()
    val showDate by homeViewModel.showDate.collectAsState(initial = DefaultSettings.SHOW_DATE)
    val showScreenTimeHome by homeViewModel.showScreenTimeHome.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_HOME)
    val showWeather by homeViewModel.showWeather.collectAsState(initial = DefaultSettings.SHOW_WEATHER)
    val showScreenTimeApp by homeViewModel.showScreenTimeApp.collectAsState(initial = DefaultSettings.SHOW_SCREEN_TIME_APP)
    val firstTimeHelp by homeViewModel.firstTimeHelp.collectAsState(initial = false)
    val homeAlignment by homeViewModel.homeAlignment.collectAsState(initial = DefaultSettingsUi.HOME_ALIGNMENT)
    val homeVAlignment by homeViewModel.homeVAlignment.collectAsState(initial = DefaultSettingsUi.HOME_V_ALIGNMENT)
    val hapticFeedbackEnabled by homeViewModel.hapticFeedBackEnabled.collectAsState(initial = DefaultSettings.HAPTIC_FEEDBACK)
    val appUsageList by homeViewModel.appUsageUiList.collectAsState()
    val favoriteApps by homeViewModel.favoriteApps.collectAsState()
    val showWallpaper by homeViewModel.showWallpaper.collectAsState(initial = false)
    val totalUsage by homeViewModel.totalUsage.collectAsState()

    val (hour, minute, _) = timeParts

    LaunchedEffect(twelveHourClock) {
        homeViewModel.startClockTicker(twelveHourClock)
    }

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
                        try {
                            @SuppressLint("WrongConstant") val service =
                                context.getSystemService("statusbar")

                            val statusBarManager = Class.forName("android.app.StatusBarManager")
                            val expand = statusBarManager.getMethod("expandNotificationsPanel")
                            expand.invoke(service)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
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

    Box(modifier.fillMaxSize()) {
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

            //Clock
            item {
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
            }

            //Glance widgets
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    glanceWidgets()

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

                    if (showWeather) {
                        if (!homeViewModel.isFoss) {
                            HomeWeatherImpl(
                                alignment = homeAlignment,
                                shadow = showWallpaper,
                                color = if (showWallpaper) MaterialTheme.colorScheme.primaryFixed else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
            }

            //Widgets
            item {
                widget()
            }

            //Apps
            items(favoriteApps, key = { app -> app.packageName }) { app ->
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

            item {
                AnimatedVisibility(
                    visible = firstTimeHelp,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(Modifier.height(15.dp))

                        FirstTimeHelp(
                            swipeForAllAppsText = stringResource(R.string.swipe_for_all_apps),
                            holdForSettingsText = stringResource(R.string.hold_for_settings)
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}


@Composable
fun HomeWeatherImpl(
    alignment: Alignment.Horizontal,
    color: Color = MaterialTheme.colorScheme.primary,
    shadow: Boolean = false,
    weatherViewModel: WeatherViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val weatherAppPackage by weatherViewModel.weatherAppPackage.collectAsState(initial = DefaultSettings.WEATHER_APP_PACKAGE)

    AnimatedVisibility(
        weatherViewModel.weatherText.value != "",
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        GlanceWidget(
            text = weatherViewModel.weatherText.value,
            icon = Icons.Default.WbSunny,
            iconContentDescription = "Weather",
            shadow = shadow,
            homeAlignment = alignment,
            small = true,
            color = color,
            onClick = {
                if (weatherAppPackage.isNotEmpty()) {
                    //todo: use OpenApp() here so it's tracked

                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(
                            weatherAppPackage
                        )
                    launchIntent?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(it)
                    }
                } else {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.set_weather_app_in_settings),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}
