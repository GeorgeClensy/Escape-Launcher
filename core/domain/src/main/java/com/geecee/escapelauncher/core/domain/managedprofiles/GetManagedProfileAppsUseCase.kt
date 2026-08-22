package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve and observe the list of apps within a managed profile.
 *
 * @property repository The repository used to fetch the apps.
 */
class GetManagedProfileAppsUseCase @Inject constructor(
    private val repository: ManagedProfileRepository
) {
    /**
     * Returns a flow of apps for the specified [ManagedProfileType].
     *
     * @param type The type of managed profile to get apps from.
     * @return A [Flow] emitting the list of [InstalledApp]s.
     * @see ManagedProfileRepository.getApps
     */
    operator fun invoke(type: ManagedProfileType): Flow<List<InstalledApp>> {
        return repository.getApps(type)
    }
}
