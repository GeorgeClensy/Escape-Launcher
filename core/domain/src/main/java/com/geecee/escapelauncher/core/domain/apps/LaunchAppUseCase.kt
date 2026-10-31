package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import jakarta.inject.Inject

/**
 * Launches an [InstalledApp] and reports the outcome.
 *
 * The platform-specific launch is delegated to [SystemActionsRepository], which handles
 * both the cross-profile `LauncherApps` path and the same-profile fallback.
 *
 * @property systemActionsRepository performs the platform-specific launch.
 * @property analyticsProxy records launch failures for diagnostics.
 */
class LaunchAppUseCase @Inject constructor(
    private val systemActionsRepository: SystemActionsRepository,
    private val analyticsProxy: AnalyticsProxy
) {
    /**
     * Launches [app].
     *
     * Failures are reported to analytics and never propagated to the caller.
     *
     * @param app the app to launch.
     * @param onAppOpened invoked with [InstalledApp.packageName] only if the launch succeeds.
     * @return `true` if the app was launched, `false` otherwise.
     */
    operator fun invoke(
        app: InstalledApp,
        onAppOpened: ((String) -> Unit)? = null
    ): Boolean {
        return try {
            systemActionsRepository.launchApp(app)
            onAppOpened?.invoke(app.packageName)
            true
        } catch (e: Exception) {
            analyticsProxy.logCustomKey("app_launch_error", app.packageName)
            analyticsProxy.recordException(e)
            false
        }
    }
}
