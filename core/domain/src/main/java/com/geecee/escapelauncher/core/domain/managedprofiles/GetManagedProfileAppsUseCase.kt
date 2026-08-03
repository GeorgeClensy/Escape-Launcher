package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetManagedProfileAppsUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    operator fun invoke(type: ManagedProfileType): Flow<List<InstalledApp>> {
        return repository.getApps(type)
    }
}
