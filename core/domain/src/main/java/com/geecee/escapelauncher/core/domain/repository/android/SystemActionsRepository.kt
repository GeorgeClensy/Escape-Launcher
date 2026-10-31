package com.geecee.escapelauncher.core.domain.repository.android

import android.content.Intent
import android.graphics.Rect
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.Flow

interface SystemActionsRepository {
    /**
     * A flow providing the current enabled state of the accessibility service.
     */
    val isAccessibilityServiceEnabled: Flow<Boolean>

    /**
     * Triggers the system uninstallation dialog for the given app.
     */
    fun uninstallApp(app: InstalledApp)

    /**
     * Opens the system settings page (App Info) for the given app.
     */
    fun openAppDetails(app: InstalledApp, sourceBounds: Rect? = null)

    /**
     * Checks if Escape Launcher is currently the default home app.
     */
    fun isDefaultLauncher(): Boolean

    /**
     * Returns an Intent to prompt the user to set Escape Launcher as the default home app.
     */
    fun getPromptDefaultLauncherIntent(): Intent

    /**
     * Sets a solid color as the system wallpaper for the home screen.
     */
    fun setSolidColorWallpaper(color: Int)

    /**
     * Locks the device screen using the accessibility service.
     */
    fun lockScreen()

    /**
     * Launches the main activity of [app].
     *
     * Uses [android.content.pm.LauncherApps] so apps in other profiles (work profile,
     * private space) can be launched. If that fails with a [SecurityException] for an app
     * belonging to the current user, a best-effort fallback through the package manager
     * launch intent is attempted. The fallback is deliberately skipped for apps in other
     * profiles, because it could launch an unrelated app with the same package name.
     *
     * @throws SecurityException if [app] belongs to a different profile and cannot be launched.
     * @throws Exception if neither the primary nor the fallback launch path succeeds.
     */
    fun launchApp(app: InstalledApp)
}
