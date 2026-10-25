package com.geecee.escapelauncher.core.data.repository.db

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.database.AppUsageDao
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.data.entity.AppUsageEntity
import com.geecee.escapelauncher.core.di.ApplicationScope
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.model.AppUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenTimeRepositoryImpl @Inject constructor(
    private val appUsageDao: AppUsageDao,
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val scope: CoroutineScope,
    @ApplicationContext private val context: Context
) : ScreenTimeRepository {
    init {
        // Register the screen-off receiver globally using application context
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                    scope.launch(Dispatchers.IO) {
                        val activePackage = getActiveSessionPackageName()
                        if (activePackage != null) {
                            onAppClosed(activePackage)
                        }
                    }
                }
            }
        }

        // Safe registration across Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
    }

    override val allUsageFlow: Flow<List<AppUsage>> = appUsageDao.getAllUsageFlow().map { entities ->
        entities.map { usage ->
            AppUsage(
                packageName = usage.packageName,
                totalTime = usage.totalTime
            )
        }
    }

    override fun onAppOpened(packageName: String) {
        val now = System.currentTimeMillis()
        // Save immediately using application scope so it outlives the caller
        scope.launch(Dispatchers.IO) {
            try {
                dataStore.edit { prefs ->
                    prefs[PreferencesKeys.ACTIVE_SESSION_PACKAGE] = packageName
                    prefs[PreferencesKeys.ACTIVE_SESSION_START_TIME] = now
                }
            } catch (e: Exception) {
                Log.e("ScreenTimeRepository", "Error saving active session start: ${e.message}")
            }
        }
    }

    override suspend fun hasActiveSession(): Boolean {
        return try {
            val prefs = dataStore.data.first()
            prefs[PreferencesKeys.ACTIVE_SESSION_PACKAGE] != null
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getActiveSessionPackageName(): String? {
        return try {
            val prefs = dataStore.data.first()
            prefs[PreferencesKeys.ACTIVE_SESSION_PACKAGE]
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun onAppClosed(packageName: String): Int {
        var openTime: Long? = null
        var activePackage: String? = null

        // Atomically read and remove the session to prevent concurrent double-counting
        try {
            dataStore.edit { prefs ->
                activePackage = prefs[PreferencesKeys.ACTIVE_SESSION_PACKAGE]
                openTime = prefs[PreferencesKeys.ACTIVE_SESSION_START_TIME]
                prefs.remove(PreferencesKeys.ACTIVE_SESSION_PACKAGE)
                prefs.remove(PreferencesKeys.ACTIVE_SESSION_START_TIME)
            }
        } catch (e: Exception) {
            Log.e("ScreenTimeRepository", "Error clearing active session: ${e.message}")
            return 0
        }

        val startTime = openTime ?: return 0
        val targetPackage = activePackage ?: packageName
        val usageTime = System.currentTimeMillis() - startTime
        if (usageTime <= 0) return 0

        val currentDate = getCurrentDate()

        return try {
            val existingUsage = appUsageDao.getAppUsage(targetPackage, currentDate)
            val updatedTime = (existingUsage?.totalTime ?: 0L) + usageTime

            appUsageDao.insertOrUpdate(
                AppUsageEntity(
                    packageName = targetPackage,
                    date = currentDate,
                    totalTime = updatedTime
                )
            )
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
            appUsageDao.deleteOldDataExcept(today, yesterday)
        } catch (e: Exception) {
            Log.e("ScreenTimeRepository", "Error clearing old data: ${e.message}")
        }
    }

    override suspend fun getTotalUsageForDate(date: String): Long {
        return appUsageDao.getTotalUsageForDate(date) ?: 0L
    }

    override fun getTotalUsageForDateFlow(date: String): Flow<Long> {
        return appUsageDao.getTotalUsageForDateFlow(date).map { it ?: 0L }
    }

    override suspend fun getUsageForApp(packageName: String, date: String): Long {
        return appUsageDao.getAppUsage(packageName, date)?.totalTime ?: 0L
    }

    override suspend fun getScreenTimeListSorted(date: String): List<AppUsage> {
        val usageList = appUsageDao.getUsageListForDate(date)
        return usageList.map { usage ->
            AppUsage(
                packageName = usage.packageName,
                totalTime = usage.totalTime
            )
        }.sortedByDescending { it.totalTime }
    }

    override fun getScreenTimeListSortedFlow(date: String): Flow<List<AppUsage>> {
        return appUsageDao.getUsageListForDateFlow(date).map { usageList ->
            usageList.map { usage ->
                AppUsage(
                    packageName = usage.packageName,
                    totalTime = usage.totalTime
                )
            }.sortedByDescending { it.totalTime }
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}