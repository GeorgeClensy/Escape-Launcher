package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import jakarta.inject.Inject

sealed class AppActionType {
    object Uninstall : AppActionType()
    data class ToggleFavorite(val isFavorite: Boolean) : AppActionType()
    object Hide : AppActionType()
    object AppInfo : AppActionType()
    object AddChallenge : AppActionType()
}

class GetAppActionsUseCase @Inject constructor(
    private val modifiedAppsRepository: ModifiedAppsRepository
) {
    operator fun invoke(packageId: String): Flow<List<AppActionType>> {
        return combine(
            modifiedAppsRepository.getFavouriteAppsInOrderFlow(),
            modifiedAppsRepository.getChallengePackageIdsFlow()
        ) { favorites, challenges ->
            val isFavorite = favorites.any { it.packageId == packageId }
            val hasChallenge = challenges.any { it == packageId }

            buildList {
                add(AppActionType.Uninstall)
                add(AppActionType.ToggleFavorite(isFavorite))
                add(AppActionType.Hide)
                add(AppActionType.AppInfo)
                if (!hasChallenge) {
                    add(AppActionType.AddChallenge)
                }
            }
        }
    }
}
