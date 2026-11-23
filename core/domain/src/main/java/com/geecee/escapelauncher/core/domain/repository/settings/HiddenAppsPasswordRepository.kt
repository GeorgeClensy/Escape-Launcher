package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface HiddenAppsPasswordRepository {
    val isPasswordSet: Flow<Boolean>
    val recoveryMinutes: Flow<Int>
    suspend fun updateRecoveryMinutes(password: String, minutes: Int): Boolean
    /** Caller must enforce a completed uninterrupted foreground recovery session before invoking. */
    suspend fun removePasswordAfterRecovery()
    suspend fun verifyPassword(password: String): Boolean

    /** Null newPassword removes protection. Existing protection requires the current password. */
    suspend fun updatePassword(currentPassword: String, newPassword: String?): Boolean

    companion object {
        const val DEFAULT_RECOVERY_MINUTES = 60
        const val MAX_RECOVERY_MINUTES = 1440
        const val MIN_PASSWORD_LENGTH = 4
        const val MAX_PASSWORD_LENGTH = 128
    }
}
