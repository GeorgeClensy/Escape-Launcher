package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import javax.inject.Inject

/**
 * A UseCase to determine if a specific managed profile type is supported for interaction
 * on the current device's API level.
 *
 * This is primarily used to decide whether to show profile-specific UI elements.
 */
class IsManagedProfileSupportedUseCase @Inject constructor(
    private val appConfiguration: AppConfiguration
) {
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
                appConfiguration.sdkInt >= MIN_SDK_PRIVATE_SPACE
            }
            ManagedProfileType.WorkApps -> {
                // Work apps needs android R to check if profile is work apps or not
                appConfiguration.sdkInt >= MIN_SDK_WORK_APPS
            }
        }
    }

    private companion object {
        const val MIN_SDK_PRIVATE_SPACE = 35 // Android 15 (Vanilla Ice Cream)
        const val MIN_SDK_WORK_APPS = 30     // Android 11 (R)
    }
}
