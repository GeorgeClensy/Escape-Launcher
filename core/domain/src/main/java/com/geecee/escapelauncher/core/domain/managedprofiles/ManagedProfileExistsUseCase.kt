package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import javax.inject.Inject

class ManagedProfileExistsUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    operator fun invoke(type: ManagedProfileType): Boolean {
        return repository.exists(type)
    }
}
