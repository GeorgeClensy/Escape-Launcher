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

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND date = :date LIMIT 1")
    suspend fun getAppUsage(packageName: String, date: String): AppUsageEntity?

    @Query("DELETE FROM app_usage WHERE date != :today AND date != :yesterday")
    suspend fun deleteOldDataExcept(today: String, yesterday: String)

    @Query("SELECT * FROM app_usage")
    suspend fun getAllUsage(): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage")
    fun getAllUsageFlow(): Flow<List<AppUsageEntity>>

    @Query("SELECT SUM(totalTime) FROM app_usage WHERE date = :date")
    suspend fun getTotalUsageForDate(date: String): Long?

    @Query("SELECT SUM(totalTime) FROM app_usage WHERE date = :date")
    fun getTotalUsageForDateFlow(date: String): Flow<Long?>

    @Query("SELECT * FROM app_usage WHERE date = :date")
    suspend fun getUsageListForDate(date: String): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE date = :date")
    fun getUsageListForDateFlow(date: String): Flow<List<AppUsageEntity>>
}
