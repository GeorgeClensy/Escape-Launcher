package com.geecee.escapelauncher.core.domain.managedprofiles

import android.os.Build
import javax.inject.Inject

/**
 * A UseCase to determine if a specific managed profile type is supported for interaction
 * on the current device's API level.
 */
class IsManagedProfileSupportedUseCase @Inject constructor() {
    operator fun invoke(type: ManagedProfileType): Boolean {
        return when (type) {
            ManagedProfileType.PrivateSpace -> {
                // Private Space is only available on Android 15+
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
            }
            ManagedProfileType.WorkApps -> {
                // Toggling work apps via LauncherApps/UserManager requestQuietModeEnabled 
                // requires API 28 (Android P)
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
            }
        }
    }
}
