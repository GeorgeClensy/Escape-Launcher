package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import javax.inject.Inject

sealed class ToggleManagedProfileUseCaseOutput {
    object SuccessfulToggleOff : ToggleManagedProfileUseCaseOutput()
    object SuccessfulToggleOn : ToggleManagedProfileUseCaseOutput()
    object FailedNotDefaultLauncher : ToggleManagedProfileUseCaseOutput()
}

class ToggleManagedProfileUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
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
