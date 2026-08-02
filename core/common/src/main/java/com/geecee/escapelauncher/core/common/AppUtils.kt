package com.geecee.escapelauncher.core.common

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Process
import android.util.Log
import androidx.core.net.toUri
import com.geecee.escapelauncher.core.analytics.analyticsProxy
import com.geecee.escapelauncher.core.model.InstalledApp

/**
 * Checks if the app belongs to the main user.
 */
fun InstalledApp.isMainUserApp(): Boolean = this.user == Process.myUserHandle()

/**
 * Function to launch an app.
 *
 * @param context Context
 * @param app The app info being opened
 * @param onAppOpened Callback called when the app is successfully opened for screen time tracking
 *
 * @return Boolean true if the app was launched successfully
 */
fun launchApp(
    context: Context,
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
        Log.e("AppUtils", "SecurityException opening app: ${e.message}")
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
            Log.e("AppUtils", "Failed to launch app even with fallback", fallbackException)
            analyticsProxy.logCustomKey("app_launch_error", app.packageName)
            analyticsProxy.recordException(fallbackException)
            false
        }
    } catch (e: Exception) {
        Log.e("AppUtils", "Error opening app", e)
        analyticsProxy.logCustomKey("app_launch_error", app.packageName)
        analyticsProxy.recordException(e)
        false
    }
}

fun uninstallApp(context: Context, app: InstalledApp) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:${app.packageName}".toUri()
        putExtra(Intent.EXTRA_USER, app.user)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun goToAppInfo(
    context: Context,
    app: InstalledApp,
    sourceBounds: Rect? = null
) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val profile = app.user

    val options = ActivityOptions.makeBasic()
    if (sourceBounds != null) {
        options.launchBounds = sourceBounds
    }
    launcherApps.startAppDetailsActivity(
        app.componentName,
        profile,
        sourceBounds,
        options.toBundle()
    )
}
