package com.geecee.escapelauncher.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geecee.escapelauncher.core.data.entity.AppUsageEntity

@Database(entities = [AppUsageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appUsageDao(): AppUsageDao
}
