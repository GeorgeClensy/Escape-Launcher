package com.geecee.escapelauncher.ui.views

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.geecee.escapelauncher.MainAppViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.composables.AppUsage
import com.geecee.escapelauncher.ui.composables.AppUsages
import com.geecee.escapelauncher.ui.composables.DaySpent
import com.geecee.escapelauncher.ui.composables.HigherRec
import com.geecee.escapelauncher.ui.composables.ScreenTime
import com.geecee.escapelauncher.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.ui.theme.ContentColor
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
    val recommendedTime: Double = 0.5 * 60 * 60 * 1000 // 1 hours in milliseconds

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
            Log.e(
                "ERROR",
                "Error fetching total screen time usage for today in ScreenTimeDashboard: ${e.message}"
            )
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
            Log.e(
                "ERROR",
                "Error fetching app screen time usages for today in ScreenTimeDashboard: ${e.message}"
            )
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
            Log.e(
                "ERROR",
                "Error fetching app screen time usages for yesterday in ScreenTimeDash: ${e.message}"
            )
        }

        // Get total usage for Yesterday
        try {
            withContext(Dispatchers.IO) {
                yesterdayUsage.longValue = getTotalUsageForDate(AppUtils.getYesterday())
            }
        } catch (e: Exception) {
            Log.e(
                "ERROR",
                "Error fetching yesterday's total screen time usages in ScreenTimeDash: ${e.message}"
            )
        }
    }

    // UI for ScreenTime screen
    Column(
        Modifier
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
                    color = ContentColor
                )
            }
        }

        Spacer(Modifier.height(15.dp))

        SettingsSpacer()
        SettingsSpacer()

    }
}
