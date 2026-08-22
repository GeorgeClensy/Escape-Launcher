package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to observe whether a managed profile is currently unlocked.
 *
 * @property repository The repository used to observe the status.
 */
class ObserveManagedProfileUnlockedUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    /**
     * Returns a flow observing the unlock status of the specified profile.
     *
     * @param type The type of managed profile to observe.
     * @return A [Flow] emitting true if unlocked, false otherwise.
     */
    operator fun invoke(type: ManagedProfileType): Flow<Boolean> {
        return repository.observeUnlocked(type)
    }
}
