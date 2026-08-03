package com.geecee.escapelauncher.core.domain.repository.db

import com.geecee.escapelauncher.core.model.AppUsage

interface ScreenTimeRepository {
    fun onAppOpened(packageName: String)
    suspend fun onAppClosed(packageName: String): Int
    fun hasActiveSession(): Boolean
    fun getActiveSessionPackageName(): String?
    suspend fun clearOldData()
    suspend fun getTotalUsageForDate(date: String): Long
    suspend fun getUsageForApp(packageName: String, date: String): Long
    suspend fun getScreenTimeListSorted(date: String): List<AppUsage>
}
