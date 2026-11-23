package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
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
    private val modifiedAppsRepository: ModifiedAppsRepository,
    private val passwordRepository: HiddenAppsPasswordRepository
) {
    operator fun invoke(): Flow<List<InstalledApp>> {
        return combine(
            appsRepository.mainUserApps,
            modifiedAppsRepository.getFavouriteAppsInOrderFlow(),
            passwordRepository.isPasswordSet
        ) { apps, entities, isProtected ->
            entities.mapNotNull { entity ->
                if (isProtected && entity.isHidden) null
                else apps.find { it.packageName == entity.packageId }
            }
        }
    }
}