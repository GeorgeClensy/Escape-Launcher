package com.geecee.escapelauncher.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity

@Database(entities = [ModifiedAppEntity::class], version = 1, exportSchema = false)
abstract class ModifiedAppsDatabase: RoomDatabase() {
    abstract fun modifiedAppsDao(): ModifiedAppsDao
}