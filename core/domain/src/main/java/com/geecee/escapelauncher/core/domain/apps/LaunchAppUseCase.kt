package com.geecee.escapelauncher.core.domain.apps

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.util.Log
import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

class LaunchAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsProxy: AnalyticsProxy
) {
    operator fun invoke(
        app: InstalledApp,
        onAppOpened: ((String) -> Unit)? = null
    ): Boolean {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val options = ActivityOptions.makeBasic()

        return try {
            launcherApps.startMainActivity(
                app.componentName,
                app.user,
                Rect(),
                options.toBundle()
            )
            onAppOpened?.invoke(app.packageName)
            true
        } catch (e: SecurityException) {
            Log.e("LaunchAppUseCase", "SecurityException opening app: ${e.message}")
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent != null) {
                    context.startActivity(intent)
                    onAppOpened?.invoke(app.packageName)
                    true
                } else {
                    false
                }
            } catch (fallbackException: Exception) {
                Log.e("LaunchAppUseCase", "Failed to launch app even with fallback", fallbackException)
                analyticsProxy.logCustomKey("app_launch_error", app.packageName)
                analyticsProxy.recordException(fallbackException)
                false
            }
        } catch (e: Exception) {
            Log.e("LaunchAppUseCase", "Error opening app", e)
            analyticsProxy.logCustomKey("app_launch_error", app.packageName)
            analyticsProxy.recordException(e)
            false
        }
    }
}
