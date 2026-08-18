package com.geecee.escapelauncher.core.domain.repository.android

import com.geecee.escapelauncher.core.model.AppShortcut
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.StateFlow

interface AppsRepository {
    /**
     * A flow providing the list of all installed applications on the device across all profiles.
     */
    val installedApps: StateFlow<List<InstalledApp>>

    /**
     * A flow providing the list of installed applications belonging to the main user profile.
     */
    val mainUserApps: StateFlow<List<InstalledApp>>

    /**
     * Triggers a manual reload of the installed applications.
     */
    fun reloadApps()

    /**
     * Retrieves the display name of an app from its package name.
     *
     * @param packageName The package name of the app.
     * @return The app's display name, or "null" if not found.
     */
    fun getAppNameFromPackageName(packageName: String): String

    /**
     * Retrieves the [InstalledApp] object corresponding to a package name.
     *
     * @param packageName The package name of the app.
     * @return The [InstalledApp] if found, otherwise null.
     */
    fun getInstalledAppFromPackageName(packageName: String): InstalledApp?

    /**
     * Retrieves the list of shortcuts available for a given package name.
     *
     * This method may return an empty list if the launcher is not the default home app.
     *
     * @param packageName The package name to query shortcuts for.
     * @return A list of [AppShortcut]s found for the package.
     */
    fun getShortcuts(packageName: String): List<AppShortcut>

    /**
     * Launches a specific shortcut for an app.
     *
     * @param packageName The package name of the app owning the shortcut.
     * @param shortcutId The unique ID of the shortcut to start.
     */
    fun startShortcut(packageName: String, shortcutId: String)
}