package com.geecee.escapelauncher.core.model

/**
 * Represents the loading state of essential data required to start the launcher.
 *
 * @property isAppsLoaded Whether the list of installed apps has been loaded from the system.
 * @property isFavoritesLoaded Whether the user's favorite apps have been loaded from the database.
 * @property isSettingsLoaded Whether the essential launcher settings have been loaded from DataStore.
 * @property isScreenTimeLoaded Whether the screen time usage and settings have been loaded.
 */
data class AppInitializationState(
    val isAppsLoaded: Boolean = false,
    val isFavoritesLoaded: Boolean = false,
    val isSettingsLoaded: Boolean = false,
    val isScreenTimeLoaded: Boolean = false
) {
    /**
     * Returns true if all essential data has been loaded and the splash screen can be dismissed.
     */
    val isAllLoaded: Boolean get() = isAppsLoaded && isFavoritesLoaded && isSettingsLoaded && isScreenTimeLoaded
}
