package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.geecee.escapelauncher.core.data.entity.TodoPendingCommandEntity
import com.geecee.escapelauncher.core.data.entity.TodoTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    // ---- tasks
    @Query("SELECT * FROM todoTasks ORDER BY childOrder ASC, updatedAt ASC")
    fun observeTasks(): Flow<List<TodoTaskEntity>>

    @Query("SELECT * FROM todoTasks WHERE id = :id LIMIT 1")
    suspend fun getTask(id: String): TodoTaskEntity?

    @Query("SELECT * FROM todoTasks WHERE checked = 0 ORDER BY childOrder ASC, updatedAt ASC")
    suspend fun getOpenTasks(): List<TodoTaskEntity>

    @Query("SELECT COALESCE(MAX(childOrder), 0) FROM todoTasks WHERE parentId IS :parentId")
    suspend fun maxChildOrder(parentId: String?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TodoTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTasks(tasks: List<TodoTaskEntity>)

    @Query("UPDATE todoTasks SET content = :content, updatedAt = :now WHERE id = :id")
    suspend fun updateContent(id: String, content: String, now: Long)

    @Query("UPDATE todoTasks SET checked = :checked, updatedAt = :now WHERE id = :id OR parentId = :id")
    suspend fun updateCheckedWithChildren(id: String, checked: Boolean, now: Long)

    @Query("UPDATE todoTasks SET checked = :checked, updatedAt = :now WHERE id = :id")
    suspend fun updateChecked(id: String, checked: Boolean, now: Long)

    @Query("DELETE FROM todoTasks WHERE id = :id OR parentId = :id")
    suspend fun deleteTaskWithChildren(id: String)

    @Query("DELETE FROM todoTasks WHERE id NOT IN (:keepIds)")
    suspend fun deleteTasksNotIn(keepIds: List<String>)

    @Query("DELETE FROM todoTasks")
    suspend fun deleteAllTasks()

    /** Completed top-level tasks older than [before] are of no use to the page any more. */
    @Query(
        """
        DELETE FROM todoTasks
        WHERE (id IN (SELECT id FROM todoTasks WHERE parentId IS NULL AND checked = 1 AND updatedAt < :before))
           OR (parentId IN (SELECT id FROM todoTasks WHERE parentId IS NULL AND checked = 1 AND updatedAt < :before))
        """
    )
    suspend fun purgeStaleCompleted(before: Long)

    @Query("UPDATE todoTasks SET id = :realId WHERE id = :tempId")
    suspend fun remapTaskId(tempId: String, realId: String)

    @Query("UPDATE todoTasks SET parentId = :realId WHERE parentId = :tempId")
    suspend fun remapTaskParentId(tempId: String, realId: String)

    // ---- pending commands
    @Query("SELECT * FROM todoPendingCommands ORDER BY seq ASC")
    suspend fun getPendingCommands(): List<TodoPendingCommandEntity>

    @Query("SELECT COUNT(*) FROM todoPendingCommands")
    suspend fun pendingCount(): Int

    @Query("SELECT COUNT(*) FROM todoPendingCommands")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT DISTINCT taskId FROM todoPendingCommands")
    suspend fun getPendingTaskIds(): List<String>

    @Insert
    suspend fun insertPending(command: TodoPendingCommandEntity)

    @Query("DELETE FROM todoPendingCommands WHERE uuid = :uuid")
    suspend fun deletePending(uuid: String)

    @Query("DELETE FROM todoPendingCommands")
    suspend fun deleteAllPending()

    @Query("UPDATE todoPendingCommands SET taskId = :realId WHERE taskId = :tempId")
    suspend fun remapPendingTaskId(tempId: String, realId: String)

    @Query("UPDATE todoPendingCommands SET parentId = :realId WHERE parentId = :tempId")
    suspend fun remapPendingParentId(tempId: String, realId: String)

    @Transaction
    suspend fun remapId(tempId: String, realId: String) {
        remapTaskId(tempId, realId)
        remapTaskParentId(tempId, realId)
        remapPendingTaskId(tempId, realId)
        remapPendingParentId(tempId, realId)
    }

    @Transaction
    suspend fun replaceAllExcept(tasks: List<TodoTaskEntity>, keepIds: List<String>) {
        // Rows with pending commands keep their local state; everything else mirrors the server.
        if (keepIds.isEmpty()) deleteAllTasks() else deleteTasksNotIn(keepIds)
        upsertTasks(tasks.filter { it.id !in keepIds })
    }

    @Transaction
    suspend fun clearAll() {
        deleteAllTasks()
        deleteAllPending()
    }
}
