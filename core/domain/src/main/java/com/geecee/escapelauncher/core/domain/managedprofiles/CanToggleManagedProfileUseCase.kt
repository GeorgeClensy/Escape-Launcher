package com.geecee.escapelauncher.core.domain.managedprofiles

import android.os.Build
import javax.inject.Inject

/**
 * A UseCase to determine if the current device's API level supports programmatically
 * toggling the "Quiet Mode" (lock/unlock) for a managed profile.
 *
 * Toggling typically requires Android 9 (Pie) or higher for Work Profiles, and
 * Android 15 (Vanilla Ice Cream) for Private Space.
 */
class CanToggleManagedProfileUseCase @Inject constructor() {
    /**
     * Checks if the lock state of the given [ManagedProfileType] can be toggled on this device.
     *
     * @param type The profile type to check.
     * @return True if toggling is supported, false otherwise.
     */
    operator fun invoke(type: ManagedProfileType): Boolean {
        return when (type) {
            ManagedProfileType.PrivateSpace -> {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
            }
            ManagedProfileType.WorkApps -> {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            }
        }
    }
}
