package com.geecee.escapelauncher.core.domain.managedprofiles

import android.os.Build
import javax.inject.Inject

/**
 * A UseCase to determine if the current device's API level supports programmatically
 * toggling the "Quiet Mode" (lock/unlock) for a managed profile.
 */
class CanToggleManagedProfileUseCase @Inject constructor() {
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
