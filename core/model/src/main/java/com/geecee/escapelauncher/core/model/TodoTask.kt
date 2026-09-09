package com.geecee.escapelauncher.core.model

/**
 * A task in the to-do list. Sub-tasks ("steps") are tasks whose [parentId] points at a top-level task.
 */
data class TodoTask(
    val id: String,
    val content: String,
    val checked: Boolean,
    val parentId: String?,
    val order: Int,
    val projectId: String
)

data class TodoProject(
    val id: String,
    val name: String,
    val isInbox: Boolean
)

/**
 * Where the to-do list stands with respect to its optional remote (Todoist).
 *
 * [State.LOCAL] is the default: tasks live on the phone only and nothing is synced.
 */
data class TodoSyncStatus(
    val state: State = State.LOCAL,
    val lastSyncedAt: Long? = null,
    val pendingCount: Int = 0,
    val message: String? = null
) {
    enum class State { LOCAL, IDLE, SYNCING, OFFLINE, AUTH_ERROR, ERROR }
}
