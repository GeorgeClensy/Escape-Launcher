package com.geecee.escapelauncher.core.domain.screentime

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.model.AppUsageUiModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Use case to process and format app usage data for display in the UI.
 * Handles joining usage stats with app names and calculating trend (increase/decrease).
 */
class GetAppUsageUiListUseCase @Inject constructor(
    private val screenTimeRepository: ScreenTimeRepository,
    private val appsRepository: AppsRepository
) {
    operator fun invoke(date: String, yesterdayDate: String): Flow<List<AppUsageUiModel>> {
        return combine(
            screenTimeRepository.getScreenTimeListSortedFlow(date),
            screenTimeRepository.getScreenTimeListSortedFlow(yesterdayDate),
            appsRepository.installedApps
        ) { todayUsage, yesterdayUsage, apps ->
            todayUsage.map { appScreenTime ->
                val yesterdayAppUsage = yesterdayUsage.find { it.packageName == appScreenTime.packageName }
                val usageIncreased = appScreenTime.totalTime > (yesterdayAppUsage?.totalTime ?: 0L)
                
                // Get display name from current installed apps
                val appName = apps.find { it.packageName == appScreenTime.packageName }?.displayName ?: "null"

                AppUsageUiModel(
                    packageName = appScreenTime.packageName,
                    appName = appName,
                    totalTime = appScreenTime.totalTime,
                    usageIncreased = usageIncreased
                )
            }.filter { it.appName != "null" }
        }
    }
}
