package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import jakarta.inject.Inject

class LaunchAppUseCase @Inject constructor(
    private val systemActionsRepository: SystemActionsRepository,
    private val analyticsProxy: AnalyticsProxy
) {
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