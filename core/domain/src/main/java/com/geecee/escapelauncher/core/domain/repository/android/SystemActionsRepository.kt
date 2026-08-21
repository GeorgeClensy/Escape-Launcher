package com.geecee.escapelauncher.core.domain.repository.android

import android.graphics.Rect
import com.geecee.escapelauncher.core.model.InstalledApp

interface SystemActionsRepository {
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
     * Prompts the user to set Escape Launcher as the default home app.
     */
    fun promptSetDefaultLauncher()

    /**
     * Sets a solid color as the system wallpaper for the home screen.
     */
    fun setSolidColorWallpaper(color: Int)

    /**
     * Locks the device screen using the accessibility service.
     */
    fun lockScreen()
}
