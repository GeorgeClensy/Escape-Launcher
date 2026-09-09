package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todoTasks")
data class TodoTaskEntity(
    @PrimaryKey val id: String,
    val content: String,
    val checked: Boolean,
    val parentId: String?,
    val childOrder: Int,
    val projectId: String,
    /** Wall-clock time of the last local or remote change, used to purge stale completed tasks. */
    val updatedAt: Long
)

/**
 * A Todoist Sync API command that has been applied locally but not yet accepted by the server.
 * Commands are replayed in [seq] order; the arguments are rebuilt from the columns at send time so
 * temp ids can be remapped after the server assigns real ones.
 */
@Entity(tableName = "todoPendingCommands")
data class TodoPendingCommandEntity(
    @PrimaryKey(autoGenerate = true) val seq: Long = 0,
    val uuid: String,
    val type: String,
    val taskId: String,
    val parentId: String?,
    val content: String?,
    val projectId: String?
)
