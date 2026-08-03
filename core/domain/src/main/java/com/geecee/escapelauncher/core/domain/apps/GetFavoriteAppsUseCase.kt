package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * A UseCase that combines currently installed apps with the user's favorite records
 * from the database to return an ordered list of favorite [com.geecee.escapelauncher.core.model.InstalledApp] objects.
 */
class GetFavoriteAppsUseCase @Inject constructor(
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) {
    operator fun invoke(): Flow<List<InstalledApp>> {
        return combine(
            appsRepository.mainUserApps,
            modifiedAppsRepository.getFavouriteAppsInOrderFlow()
        ) { apps, entities ->
            entities.mapNotNull { entity ->
                apps.find { it.packageName == entity.packageId }
            }
        }
    }
}