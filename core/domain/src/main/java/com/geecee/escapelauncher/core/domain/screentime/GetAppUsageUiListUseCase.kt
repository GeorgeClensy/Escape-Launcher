package com.geecee.escapelauncher.core.domain.screentime

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.model.AppUsageUiModel
import jakarta.inject.Inject

/**
 * Use case to process and format app usage data for display in the UI.
 * Handles joining usage stats with app names and calculating trend (increase/decrease).
 */
class GetAppUsageUiListUseCase @Inject constructor(
    private val screenTimeRepository: ScreenTimeRepository,
    private val appsRepository: AppsRepository
) {
    suspend operator fun invoke(date: String, yesterdayDate: String): List<AppUsageUiModel> {
        val todayUsage = screenTimeRepository.getScreenTimeListSorted(date)
        val yesterdayUsage = screenTimeRepository.getScreenTimeListSorted(yesterdayDate)

        return todayUsage.map { appScreenTime ->
            val yesterdayAppUsage = yesterdayUsage.find { it.packageName == appScreenTime.packageName }
            val usageIncreased = appScreenTime.totalTime > (yesterdayAppUsage?.totalTime ?: 0L)
            val appName = appsRepository.getAppNameFromPackageName(appScreenTime.packageName)

            AppUsageUiModel(
                packageName = appScreenTime.packageName,
                appName = appName,
                totalTime = appScreenTime.totalTime,
                usageIncreased = usageIncreased
            )
        }.filter { it.appName != "null" }
    }
}
