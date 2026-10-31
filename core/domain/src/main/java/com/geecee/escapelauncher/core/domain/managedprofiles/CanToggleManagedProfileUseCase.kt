package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import javax.inject.Inject

/**
 * A UseCase to determine if the current device's API level supports programmatically
 * toggling the "Quiet Mode" (lock/unlock) for a managed profile.
 *
 * Toggling typically requires Android 9 (Pie) or higher for Work Profiles, and
 * Android 15 (Vanilla Ice Cream) for Private Space.
 */
class CanToggleManagedProfileUseCase @Inject constructor(
    private val appConfiguration: AppConfiguration
) {
    /**
     * Checks if the lock state of the given [ManagedProfileType] can be toggled on this device.
     *
     * @param type The profile type to check.
     * @return True if toggling is supported, false otherwise.
     */
    operator fun invoke(type: ManagedProfileType): Boolean {
        return when (type) {
            ManagedProfileType.PrivateSpace -> {
                appConfiguration.sdkInt >= MIN_SDK_PRIVATE_SPACE
            }

            ManagedProfileType.WorkApps -> {
                appConfiguration.sdkInt >= MIN_SDK_WORK_PROFILE
            }
        }
    }

    private companion object {
        const val MIN_SDK_PRIVATE_SPACE = 35 // Android 15 (Vanilla Ice Cream)
        const val MIN_SDK_WORK_PROFILE = 28  // Android 9 (Pie)
    }
}
