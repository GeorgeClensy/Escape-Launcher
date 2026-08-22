package com.geecee.escapelauncher.core.domain.managedprofiles

import android.os.Build
import javax.inject.Inject

/**
 * A UseCase to determine if a specific managed profile type is supported for interaction
 * on the current device's API level.
 *
 * This is primarily used to decide whether to show profile-specific UI elements.
 */
class IsManagedProfileSupportedUseCase @Inject constructor() {
    /**
     * Checks if the given [ManagedProfileType] is supported on this device's OS version.
     *
     * @param type The profile type to check.
     * @return True if supported, false otherwise.
     */
    operator fun invoke(type: ManagedProfileType): Boolean {
        return when (type) {
            ManagedProfileType.PrivateSpace -> {
                // Private Space is only available on Android 15+
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
            }
            ManagedProfileType.WorkApps -> {
                // Work apps are supported for viewing on all versions this launcher supports (API 26+)
                true
            }
        }
    }
}
