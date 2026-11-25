package com.geecee.escapelauncher.core.domain.startup

import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.model.AppInitializationState
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

/**
 * A UseCase that monitors the loading state of essential data required to start the launcher.
 *
 * It combines signals from:
 * - [AppsRepository]: To ensure installed apps are indexed.
 * - [ModifiedAppsRepository]: To ensure favorite apps are loaded.
 * - [AppearanceRepository]: To ensure user settings are ready.
 * - [ScreenTimeRepository] & [ScreenTimeSettingsRepository]: To ensure screen time data is ready.
 * - [AppStartupMigrationUseCase]: To ensure legacy SharedPreferences migration has finished.
 */
class GetAppInitializationStateUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val appearanceRepository: AppearanceRepository,
    private val screenTimeRepository: ScreenTimeRepository,
    private val screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    private val appStartupMigrationUseCase: AppStartupMigrationUseCase,
    private val analyticsProxy: AnalyticsProxy
) {
    /**
     * Returns a [Flow] of [AppInitializationState] representing the current loading progress.
     */
    operator fun invoke(): Flow<AppInitializationState> {
        val migrationFlow = flow {
            emit(false)
            try {
                appStartupMigrationUseCase()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                analyticsProxy.recordException(e)
            }
            emit(true)
        }

        return combine(
            appsRepository.installedApps.map { it.isNotEmpty() },
            modifiedAppsRepository.getFavouriteAppsInOrderFlow().map { true },
            appearanceRepository.showStatusBar.map { true },
            combine(
                screenTimeRepository.allUsageFlow.map { true },
                screenTimeSettingsRepository.hideScreenTimePage.map { true }
            ) { usageReady, settingsReady -> usageReady && settingsReady },
            migrationFlow
        ) { appsLoaded, favoritesLoaded, settingsLoaded, screenTimeLoaded, migrationComplete ->
            AppInitializationState(
                isAppsLoaded = appsLoaded,
                isFavoritesLoaded = favoritesLoaded,
                isSettingsLoaded = settingsLoaded,
                isScreenTimeLoaded = screenTimeLoaded,
                isMigrationComplete = migrationComplete
            )
        }
    }
}
