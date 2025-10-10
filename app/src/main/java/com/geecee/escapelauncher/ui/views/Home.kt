package com.geecee.escapelauncher.ui.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.AppUtils.doHapticFeedBack
import com.geecee.escapelauncher.utils.AppUtils.formatScreenTime
import com.geecee.escapelauncher.utils.AppUtils.getCurrentTime
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
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
                    homeAlignment = getHomeAlignment(mainAppModel.getContext())
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
        items(homeScreenModel.favoriteApps) { app ->
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

/**
 * Clock to be shown on home screen
 */
@Composable
fun Clock(
    bigClock: Boolean, homeAlignment: Alignment.Horizontal, twelveHour: Boolean
) {
    var time by remember { mutableStateOf(getCurrentTime(twelveHour)) }
    val parts = time.split(":")
    val hours = parts[0]
    val minutes = parts[1]

    LaunchedEffect(Unit) {
        while (true) {
            time = getCurrentTime(twelveHour)
            delay(1000) // Update every second
        }
    }

    if (bigClock) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(0.dp, 15.dp)
        ) {
            // Hours row
            Row {
                // Ensure hours has two digits
                val hourDigits = if (hours.length == 1) "0$hours" else hours

                hourDigits.forEachIndexed { index, digit ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(40.dp)
                            .offset(0.dp, 30.dp)
                    ) {
                        Text(
                            text = digit.toString(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Minutes row
            Row {
                // Ensure minutes has two digits
                val minuteDigits = if (minutes.length == 1) "0$minutes" else minutes

                minuteDigits.forEachIndexed { index, digit ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.width(40.dp)
                    ) {
                        Text(
                            text = digit.toString(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    } else {
        Text(
            text = time,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = when (homeAlignment) {
                Alignment.Start -> TextAlign.Start
                Alignment.End -> TextAlign.End
                else -> TextAlign.Center
            }
        )
    }
}

@Composable
fun Date(
    homeAlignment: Alignment.Horizontal
) {
    val dateFormat = SimpleDateFormat("EEE d MMM", Locale.getDefault())

    fun getCurrentDate(): String {
        return dateFormat.format(java.util.Date())
    }

    var date by remember { mutableStateOf(getCurrentDate()) }

    LaunchedEffect(Unit) {
        while (true) {
            date = getCurrentDate()
            delay(1000)
        }
    }

    Text(
        text = date,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding((7.5).dp, 0.dp, 0.dp, (7.5).dp),
        textAlign = when (homeAlignment) {
            Alignment.Start -> TextAlign.Start
            Alignment.End -> TextAlign.End
            else -> TextAlign.Center
        }
    )
}

/**
 * Screen time on home screen
 */
@Composable
fun HomeScreenScreenTime(
    screenTime: String
) {
    Box(
        Modifier
            .clip(
                MaterialTheme.shapes.extraLarge
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(0.dp,(7.5).dp)
    ) {
        Text(
            text = screenTime,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(25.dp)
        )
    }
}

/**
 * Block with tips for first time users
 */
@Composable
fun FirstTimeHelp() {
    Box(
        Modifier.clip(
            MaterialTheme.shapes.extraLarge
        )
    ) {
        Column(
            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                Modifier
                    .padding(25.dp, 25.dp, 25.dp, 15.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.swipe_for_all_apps),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(
                Modifier
                    .padding(25.dp, 0.dp, 25.dp, 25.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Icon(
                    Icons.Default.Settings,
                    "",
                    Modifier.align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    stringResource(R.string.hold_for_settings),
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
