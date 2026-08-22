package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geecee.escapelauncher.core.data.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(appUsage: AppUsageEntity)

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName")
    suspend fun getAppUsage(packageName: String): AppUsageEntity?

    @Query("DELETE FROM app_usage WHERE packageName NOT LIKE :todaySuffix AND packageName NOT LIKE :yesterdaySuffix")
    suspend fun deleteOldDataExcept(todaySuffix: String, yesterdaySuffix: String)

    @Query("SELECT * FROM app_usage")
    suspend fun getAllUsage(): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage")
    fun getAllUsageFlow(): Flow<List<AppUsageEntity>>

    @Query("SELECT SUM(totalTime) FROM app_usage WHERE packageName LIKE :dateSuffix")
    suspend fun getTotalUsageForDate(dateSuffix: String): Long?

    @Query("SELECT SUM(totalTime) FROM app_usage WHERE packageName LIKE :dateSuffix")
    fun getTotalUsageForDateFlow(dateSuffix: String): Flow<Long?>

    @Query("SELECT * FROM app_usage WHERE packageName LIKE :dateSuffix")
    suspend fun getUsageListForDate(dateSuffix: String): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE packageName LIKE :dateSuffix")
    fun getUsageListForDateFlow(dateSuffix: String): Flow<List<AppUsageEntity>>
}
