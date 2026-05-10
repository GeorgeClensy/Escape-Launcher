package com.geecee.escapelauncher.core.common

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Process
import androidx.core.net.toUri
import com.geecee.escapelauncher.core.model.InstalledApp

/**
 * Checks if the app belongs to the main user.
 */
fun InstalledApp.isMainUserApp(): Boolean = this.user == Process.myUserHandle()

/**
 * Opens an app regardless of its profile (Main, Work, or Private).
 */
fun openApp(context: Context, app: InstalledApp, sourceBounds: Rect? = null) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val options = ActivityOptions.makeBasic()
    if (sourceBounds != null) {
        options.launchBounds = sourceBounds
    }
    
    try {
        launcherApps.startMainActivity(
            app.componentName,
            app.user,
            sourceBounds,
            options.toBundle()
        )
    } catch (e: Exception) {
        // Fallback or logging if needed
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