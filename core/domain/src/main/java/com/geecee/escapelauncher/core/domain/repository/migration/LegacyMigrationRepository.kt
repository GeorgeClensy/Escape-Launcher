package com.geecee.escapelauncher.core.domain.repository.migration

import com.geecee.escapelauncher.core.model.ModifiedApp

interface LegacyMigrationRepository {
    suspend fun isLegacyAppsMigrated(): Boolean
    suspend fun setLegacyAppsMigrated(migrated: Boolean)
    suspend fun readLegacyModifiedApps(): List<ModifiedApp>
    suspend fun clearLegacySharedPreferences()
}
