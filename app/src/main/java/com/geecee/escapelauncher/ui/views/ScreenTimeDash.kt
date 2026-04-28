package com.geecee.escapelauncher.ui.views

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.core.ui.composables.AppUsage
import com.geecee.escapelauncher.core.ui.composables.AppUsages
import com.geecee.escapelauncher.core.ui.composables.ScreenTime
import com.geecee.escapelauncher.core.ui.composables.ScreenTimeInfoBox
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.theme.ContentColor
import com.geecee.escapelauncher.core.theme.escapeGreen
import com.geecee.escapelauncher.core.theme.escapeRed
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.feature.screentime.ScreenTimeViewModel

/**
 * This function works out if the screen time is over the recommended and if it is finds out how many percent over it is
 */
fun calculateOveragePercentage(screenTime: Long): Int {
    val recommendedTime: Double = 0.5 * 60 * 60 * 1000 // 1 hour in milliseconds

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
 */
@Composable
fun ScreenTimeDashboard(
    screenTimeViewModel: ScreenTimeViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val context = LocalActivity.current as Context
    val todayUsage by screenTimeViewModel.totalUsage.collectAsState()
    val yesterdayUsage by screenTimeViewModel.yesterdayTotalUsage.collectAsState()
    val appUsageToday by screenTimeViewModel.appUsageList.collectAsState()
    val appUsageYesterday by screenTimeViewModel.yesterdayAppUsageList.collectAsState()

    // UI for ScreenTime screen
    Column(
        Modifier
            .fillMaxSize()
            .padding(15.dp, 0.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(120.dp))

        ScreenTime(
            AppUtils.formatScreenTime(todayUsage),
            todayUsage > yesterdayUsage,
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

            val percentOfYourDayOnYourPhone = ((todayUsage.toDouble() / totalMs) * 100).toInt()
            ScreenTimeInfoBox(
                text = stringResource(R.string.of_your_day_spent_on_your_phone),
                percent = ((todayUsage.toDouble() / totalMs) * 100).toInt(),
                percentageColour = if (percentOfYourDayOnYourPhone < 10) escapeGreen else escapeRed,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )

            val percentHigherThanRec = calculateOveragePercentage(todayUsage)
            ScreenTimeInfoBox(
                text = stringResource(R.string.higher_we_rec),
                percent = percentHigherThanRec,
                percentageColour = if (percentHigherThanRec < 1) escapeGreen else escapeRed,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
        }

        Spacer(Modifier.height(15.dp))

        AppUsages(Modifier) {
            if (appUsageToday.isNotEmpty()) {
                appUsageToday.forEach { appScreenTime ->
                    val appName = AppUtils.getAppNameFromPackageName(context, appScreenTime.packageName)
                    if (appName != "null") {
                        val yesterdayAppUsage = appUsageYesterday.find { it.packageName == appScreenTime.packageName }
                        val usageIncreased = appScreenTime.totalTime > (yesterdayAppUsage?.totalTime ?: 0L)

                        AppUsage(
                            appName,
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
