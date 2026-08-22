package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import javax.inject.Inject

/**
 * Represents the possible outcomes of attempting to toggle a managed profile.
 */
sealed class ToggleManagedProfileUseCaseOutput {
    /** The profile was successfully locked (turned off). */
    object SuccessfulToggleOff : ToggleManagedProfileUseCaseOutput()

    /** The profile was successfully unlocked (turned on). */
    object SuccessfulToggleOn : ToggleManagedProfileUseCaseOutput()

    /** The toggle failed because the app is not the default launcher. */
    object FailedNotDefaultLauncher : ToggleManagedProfileUseCaseOutput()
}

/**
 * Use case to toggle the lock/unlock state of a managed profile.
 *
 * @property repository The repository used to perform the toggle operation.
 */
class ToggleManagedProfileUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    /**
     * Toggles the lock state of the specified [ManagedProfileType].
     * If the profile is unlocked, it will be locked, and vice versa.
     *
     * @param type The type of managed profile to toggle.
     * @return A [ToggleManagedProfileUseCaseOutput] indicating the result of the operation.
     */
    suspend operator fun invoke(type: ManagedProfileType): ToggleManagedProfileUseCaseOutput {
        if (!repository.isDefaultLauncher()) {
            return ToggleManagedProfileUseCaseOutput.FailedNotDefaultLauncher
        }

        return if (repository.isUnlocked(type)) {
            repository.lock(type)
            ToggleManagedProfileUseCaseOutput.SuccessfulToggleOff
        } else {
            repository.unlock(type)
            ToggleManagedProfileUseCaseOutput.SuccessfulToggleOn
        }
    }
}
