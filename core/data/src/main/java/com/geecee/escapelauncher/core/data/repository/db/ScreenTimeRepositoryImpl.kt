package com.geecee.escapelauncher.core.data.repository.db

import android.util.Log
import com.geecee.escapelauncher.core.data.database.AppUsageDao
import com.geecee.escapelauncher.core.data.entity.AppUsageEntity
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.model.AppUsage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeRepositoryImpl @Inject constructor(
    private val appUsageDao: AppUsageDao
) : ScreenTimeRepository {
    private val appSessions = ConcurrentHashMap<String, Long>()

    override fun onAppOpened(packageName: String) {
        appSessions[packageName] = System.currentTimeMillis()
    }

    override fun hasActiveSession(): Boolean {
        return appSessions.isNotEmpty()
    }

    override fun getActiveSessionPackageName(): String? {
        return appSessions.keys().asSequence().firstOrNull()
    }

    override suspend fun onAppClosed(packageName: String): Int {
        val openTime = appSessions[packageName] ?: return 0
        val usageTime = System.currentTimeMillis() - openTime
        val currentDate = getCurrentDate()
        val appKey = "$packageName-$currentDate"

        return try {
            val existingUsage = appUsageDao.getAppUsage(appKey)
            val updatedTime = (existingUsage?.totalTime ?: 0L) + usageTime

            appUsageDao.insertOrUpdate(
                AppUsageEntity(
                    packageName = appKey,
                    totalTime = updatedTime
                )
            )
            appSessions.remove(packageName)
            1
        } catch (e: Exception) {
            Log.e("ScreenTimeRepository", "Error saving app usage: ${e.message}")
            0
        }
    }

    override suspend fun clearOldData() {
        val today = getCurrentDate()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        try {
            appUsageDao.deleteOldDataExcept("%-$today", "%-$yesterday")
        } catch (e: Exception) {
            Log.e("ScreenTimeRepository", "Error clearing old data: ${e.message}")
        }
    }

    override suspend fun getTotalUsageForDate(date: String): Long {
        return appUsageDao.getTotalUsageForDate("%-$date") ?: 0L
    }

    override suspend fun getUsageForApp(packageName: String, date: String): Long {
        return appUsageDao.getAppUsage("$packageName-$date")?.totalTime ?: 0L
    }

    override suspend fun getScreenTimeListSorted(date: String): List<AppUsage> {
        val usageList = appUsageDao.getUsageListForDate("%-$date")
        return usageList.map { usage ->
            AppUsage(
                packageName = usage.packageName.substringBeforeLast("-$date"),
                totalTime = usage.totalTime
            )
        }.sortedByDescending { it.totalTime }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
