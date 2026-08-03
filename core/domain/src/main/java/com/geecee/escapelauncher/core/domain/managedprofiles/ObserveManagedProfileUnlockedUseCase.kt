package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveManagedProfileUnlockedUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    operator fun invoke(type: ManagedProfileType): Flow<Boolean> {
        return repository.observeUnlocked(type)
    }
}
