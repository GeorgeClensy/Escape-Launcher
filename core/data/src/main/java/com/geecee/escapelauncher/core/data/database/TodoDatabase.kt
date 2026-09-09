package com.geecee.escapelauncher.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geecee.escapelauncher.core.data.entity.TodoPendingCommandEntity
import com.geecee.escapelauncher.core.data.entity.TodoTaskEntity

@Database(
    entities = [TodoTaskEntity::class, TodoPendingCommandEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
