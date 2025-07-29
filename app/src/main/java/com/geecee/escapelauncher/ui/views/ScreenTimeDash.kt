package com.geecee.escapelauncher.ui.views

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.escapeGreen
import com.geecee.escapelauncher.ui.theme.escapeRed
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.managers.AppUsageEntity
import com.geecee.escapelauncher.utils.managers.getScreenTimeListSorted
import com.geecee.escapelauncher.utils.managers.getTotalUsageForDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * This function works out if the screen time is over the recommended and if it is finds out how many percent over it is
 */
fun calculateOveragePercentage(screenTime: Long): Int {
    val recommendedTime: Long = 2 * 60 * 60 * 1000 // 2 hours in milliseconds

    // If screen time is less than or equal to the recommended time, return 0%
    if (screenTime <= recommendedTime) {
        return 0
    }

    // Calculate the overage percentage
    val overage = screenTime - recommendedTime
    val percentage = (overage.toFloat() / recommendedTime) * 100

    return percentage.toInt()
}

/**
 * Parent UI for ScreenTimeDashboard
 * Also contains code to retrieve total screen time today, total screen time yesterday, app screen time list today and apps screen time list yesterday
 */
@Composable
fun ScreenTimeDashboard(context: Context, mainAppModel: MainAppViewModel) {
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // Retrieves data in in a subroutine
    val todayUsage = remember { mutableLongStateOf(0L) }
    val yesterdayUsage = remember { mutableLongStateOf(0L) }
    val appUsageToday = remember { mutableStateListOf<AppUsageEntity>() }
    val appUsageYesterday = remember { mutableStateListOf<AppUsageEntity>() }
    LaunchedEffect(mainAppModel.shouldReloadScreenTime.value) {
        // Get total usage for today
        try {
            withContext(Dispatchers.IO) {
                val usage = getTotalUsageForDate(today)
                withContext(Dispatchers.Main) {
                    todayUsage.longValue = usage
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Error fetching total screen time usage for today in ScreenTimeDashboard: ${e.message}")
        }

        // Get app usage list for today
        try {
            withContext(Dispatchers.IO) {
                val usageList = getScreenTimeListSorted(today)
                withContext(Dispatchers.Main) {
                    appUsageToday.clear()
                    appUsageToday.addAll(usageList)
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Error fetching app screen time usages for today in ScreenTimeDashboard: ${e.message}")
        }

        // Get app usage list for yesterday
        try {
            withContext(Dispatchers.IO) {
                val usageList = getScreenTimeListSorted(AppUtils.getYesterday())
                withContext(Dispatchers.Main) {
                    appUsageYesterday.clear()
                    appUsageYesterday.addAll(usageList)
                }
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Error fetching app screen time usages for yesterday in ScreenTimeDash: ${e.message}")
        }

        // Get total usage for Yesterday
        try {
            withContext(Dispatchers.IO) {
                yesterdayUsage.longValue = getTotalUsageForDate(AppUtils.getYesterday())
            }
        } catch (e: Exception) {
            Log.e("ERROR", "Error fetching yesterday's total screen time usages in ScreenTimeDash: ${e.message}")
        }
    }

    // UI for ScreenTime screen
    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(15.dp, 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(120.dp))

        ScreenTime(
            AppUtils.formatScreenTime(todayUsage.longValue),
            todayUsage.longValue > yesterdayUsage.longValue,
            Modifier
        )

        Spacer(Modifier.height(15.dp))

        Row(
            Modifier
                .widthIn(max = 400.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            val totalDayHours = 16
            val totalMs = totalDayHours * 60L * 60 * 1000
            DaySpent(
                ((todayUsage.longValue.toDouble() / totalMs) * 100).toInt(),
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )

            HigherRec(
                calculateOveragePercentage(todayUsage.longValue),
                Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
        }

        Spacer(Modifier.height(15.dp))

        AppUsages(Modifier) {
            if (!appUsageToday.isEmpty()) {
                appUsageToday.forEach { appScreenTime ->
                    if (AppUtils.getAppNameFromPackageName(
                            context,
                            appScreenTime.packageName
                        ) != "null"
                    ) {
                        val yesterdayAppUsage =
                            appUsageYesterday.find { it.packageName == appScreenTime.packageName }
                        val usageIncreased =
                            appScreenTime.totalTime > (yesterdayAppUsage?.totalTime ?: 0L)

                        AppUsage(
                            AppUtils.getAppNameFromPackageName(context, appScreenTime.packageName),
                            usageIncreased,
                            if (appScreenTime.totalTime > 60000) AppUtils.formatScreenTime(
                                appScreenTime.totalTime
                            ) else "<1m",
                            Modifier
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.no_apps_used),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(Modifier.height(15.dp))
    }
}

/**
 * Screen time with an arrow indicating whether it's increased or decreased
 */
@Composable
fun ScreenTime(time: String, increased: Boolean, modifier: Modifier) {
    Row {
        Icon(
            Icons.Default.KeyboardArrowUp, contentDescription = "Arrow", tint = if (increased) {
                escapeRed
            } else {
                escapeGreen
            }, modifier = Modifier
                .size(45.dp)
                .align(Alignment.CenterVertically)
                .rotate(
                    if (increased) {
                        0f
                    } else {
                        180f
                    }
                )
        )

        Spacer(Modifier.width(5.dp))

        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )

    }
}

/**
* Square shaped composable showing how many percent higher screen time is than recommended using [calculateOveragePercentage] function
*/
@Composable
fun HigherRec(percent: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints (
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .aspectRatio(1f)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        val boxWithConstraintsScope = this
        val padding = boxWithConstraintsScope.maxWidth * 0.1f
        val titleFontSize = boxWithConstraintsScope.maxWidth * 0.3f
        val bodyFontSize = boxWithConstraintsScope.maxWidth * 0.1f

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Percent Text
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = with(LocalDensity.current) { titleFontSize.toSp() },
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (percent < 1) escapeGreen else escapeRed
            )

            // Description Text
            Text(
                text = stringResource(R.string.higher_we_rec),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = with(LocalDensity.current) { bodyFontSize.toSp() },
                    lineHeight = with(LocalDensity.current) { (bodyFontSize + 5.dp).toSp() },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Circular composable showing what percent of your day was spent on your phone
 */
@Composable
fun DaySpent(percent: Int, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        val boxWithConstraintsScope = this
        val padding = boxWithConstraintsScope.maxWidth * 0.1f
        val titleFontSize = boxWithConstraintsScope.maxWidth * 0.3f
        val bodyFontSize = boxWithConstraintsScope.maxWidth * 0.08f

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Percent Text
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = with(LocalDensity.current) { titleFontSize.toSp() },
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (percent < 10) escapeGreen else escapeRed
            )

            // Description Text
            Text(
                text = stringResource(R.string.of_your_day_spent_on_your_phone),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = with(LocalDensity.current) { bodyFontSize.toSp() },
                    lineHeight = with(LocalDensity.current) { (bodyFontSize + 5.dp).toSp() },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Shows usage for a specific app with an arrow to whether its increased or decreased
 */
@Composable
fun AppUsage(appName: String, increased: Boolean, time: String, modifier: Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(0.dp, 5.dp)
    ) {
        Text(
            text = if (appName.length > 12) appName.take(12) + "..." else appName,
            modifier = Modifier.align(Alignment.CenterStart),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Row(
            Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.KeyboardArrowUp, contentDescription = "Arrow", tint = if (increased) {
                    escapeRed
                } else {
                    escapeGreen
                }, modifier = Modifier
                    .size(45.dp)
                    .align(Alignment.CenterVertically)
                    .rotate(
                        if (increased) {
                            0f
                        } else {
                            180f
                        }
                    )
            )

            Spacer(Modifier.width(5.dp))


            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Box with [AppUsage]s in it
 */
@Composable
fun AppUsages(modifier: Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(
            Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}