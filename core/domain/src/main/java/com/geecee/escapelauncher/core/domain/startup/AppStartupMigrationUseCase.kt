package com.geecee.escapelauncher.core.domain.startup

import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.migration.LegacyMigrationRepository
import jakarta.inject.Inject

class AppStartupMigrationUseCase @Inject constructor(
    private val legacyMigrationRepository: LegacyMigrationRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) {
    suspend operator fun invoke() {
        // Reading DataStore first also ensures SharedPreferencesToDataStoreMigration
        // has finished copying settings keys before we clean up the SP files.
        if (legacyMigrationRepository.isLegacyAppsMigrated()) return

        val appsToMigrate = legacyMigrationRepository.readLegacyModifiedApps()
        if (appsToMigrate.isNotEmpty()) {
            modifiedAppsRepository.upsertAll(appsToMigrate)
        }

        // Only mark done and delete legacy files if Room upsert succeeded
        // This works because this UseCase is run in a try catch and upsertAll does not catch exceptions so if it fails, it will throw an exception and never get to this line
        legacyMigrationRepository.setLegacyAppsMigrated(true)
        legacyMigrationRepository.clearLegacySharedPreferences()
    }
}
