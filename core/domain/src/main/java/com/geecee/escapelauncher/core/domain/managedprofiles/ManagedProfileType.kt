package com.geecee.escapelauncher.core.domain.managedprofiles

/**
 * Represents the different types of managed profiles supported by the launcher.
 * Managed profiles allow for isolation of apps and data within the Android system.
 */
sealed class ManagedProfileType {
    /**
     * Represents the Android 15+ "Private Space" feature, which allows users to create
     * a separate, hidden space for sensitive apps.
     */
    object PrivateSpace: ManagedProfileType()

    /**
     * Represents a standard "Work Profile" or "Managed Profile", commonly used for separating
     * business apps from personal apps.
     */
    object WorkApps: ManagedProfileType()
}