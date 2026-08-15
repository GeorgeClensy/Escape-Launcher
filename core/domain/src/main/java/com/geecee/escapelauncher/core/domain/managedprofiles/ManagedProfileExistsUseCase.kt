package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import javax.inject.Inject

/**
 * Use case to check if a specific managed profile type exists on the device.
 *
 * @property repository The repository used to query profile information.
 */
class ManagedProfileExistsUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    /**
     * Executes the check for profile existence.
     *
     * @param type The type of managed profile to check.
     * @return True if the profile exists, false otherwise.
     */
    operator fun invoke(type: ManagedProfileType): Boolean {
        return repository.exists(type)
    }
}
