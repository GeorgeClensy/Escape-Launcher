package com.geecee.escapelauncher.ui.views

import android.annotation.SuppressLint
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.BuildConfig
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.ui.composables.GlanceWidget
import com.geecee.escapelauncher.ui.composables.Clock
import com.geecee.escapelauncher.ui.composables.FirstTimeHelp
import com.geecee.escapelauncher.ui.composables.HomeScreenItem
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime
import com.geecee.escapelauncher.utils.AppUtils.resetHome
import com.geecee.escapelauncher.utils.WidgetsScreen
import com.geecee.escapelauncher.utils.analyticsProxy
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.getHomeAlignment
import com.geecee.escapelauncher.utils.getHomeVAlignment
import com.geecee.escapelauncher.utils.getStringSetting
import com.geecee.escapelauncher.utils.getWidgetHeight
import com.geecee.escapelauncher.utils.getWidgetOffset
import com.geecee.escapelauncher.utils.getWidgetWidth
import com.geecee.escapelauncher.utils.managers.getTotalUsageForDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
                                mainAppModel.getContext()
                                    .getSystemService("statusbar") // Use literal string "statusbar"

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

    LazyColumn(
        state = scrollState,
        verticalArrangement = getHomeVAlignment(mainAppModel.getContext()),
        horizontalAlignment = getHomeAlignment(mainAppModel.getContext()),
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
                    twelveHour = getBooleanSetting(
                        mainAppModel.getContext(), stringResource(R.string.twelve_hour_clock), false
                    )
                )
            }
        }

        //Glace widgets
        item {
            FlowRow (
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val context = mainAppModel.getContext()
                val homeAlignment = getHomeAlignment(context)

                if (getBooleanSetting(context, stringResource(R.string.show_date), false)) {
                    val dateFormat = remember { SimpleDateFormat("EEE d MMM", Locale.getDefault()) }
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
                            delay(delayMillis)
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
                                analyticsProxy.recordException(e)
                            }
                        }
                    )

                }

                if (getBooleanSetting(context, stringResource(R.string.ScreenTimeOnHome), false)) {
                    val todayUsage = remember { mutableLongStateOf(0L) }
                    LaunchedEffect(mainAppModel.shouldReloadScreenTime.value) {
                        withContext(Dispatchers.IO) {
                            val usage = getTotalUsageForDate(mainAppModel.getToday())
                            withContext(Dispatchers.Main) {
                                todayUsage.longValue = usage
                            }
                        }
                    }

                    GlanceWidget(
                        text = formatScreenTime(todayUsage.longValue),
                        icon = Icons.Default.Timer,
                        iconContentDescription = "Screen Time",
                        homeAlignment = homeAlignment,
                        small = true,
                        onClick = {}
                    )

                }

                if (getBooleanSetting(context, stringResource(R.string.show_weather), false)) {
                    @Suppress("KotlinConstantConditions")
                    if (!BuildConfig.IS_FOSS) {
                        LaunchedEffect(Unit) {
                            mainAppModel.updateWeather()
                        }

                        AnimatedVisibility(
                            mainAppModel.weatherText.value != "",
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            GlanceWidget(
                                text = mainAppModel.weatherText.value,
                                icon = Icons.Default.WbSunny,
                                iconContentDescription = "Weather",
                                homeAlignment = homeAlignment,
                                small = true,
                                onClick = {
                                    val weatherAppPackage = getStringSetting(
                                        context,
                                        context.getString(R.string.weather_app_package),
                                        ""
                                    )
                                    if (weatherAppPackage.isNotEmpty()) {
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
                                            context.getString(R.string.set_weather_app_in_settings),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )

                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(10.dp))
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
                            (widgetOffset.dp).toPx().toInt(), 0
                        )
                    }
                    .size(
                        (getWidgetWidth(mainAppModel.getContext())).dp,
                        (getWidgetHeight(mainAppModel.getContext())).dp
                    )
                    .padding(0.dp, 0.dp))
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
                    doHapticFeedBack(hapticFeedback = haptics)
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
