package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import javax.inject.Inject

/**
 * Use case to check if a managed profile is currently unlocked.
 *
 * @property repository The repository used to check the status.
 */
class IsManagedProfileUnlockedUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    /**
     * Returns whether the specified managed profile is currently unlocked.
     *
     * @param type The type of managed profile to check.
     * @return True if the profile is unlocked, false otherwise.
     */
    operator fun invoke(type: ManagedProfileType): Boolean {
        return repository.isUnlocked(type)
    }
}
