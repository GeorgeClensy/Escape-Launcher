package com.geecee.escapelauncher.feature.onboarding.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) : ViewModel() {
    val installedApps = appsRepository.mainUserApps

    val favoriteApps: StateFlow<List<InstalledApp>> = combine(
        appsRepository.mainUserApps,
        modifiedAppsRepository.getFavouriteAppsInOrderFlow()
    ) { apps, entities ->
        entities.mapNotNull { entity ->
            apps.find { it.packageName == entity.packageId }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addFavorite(packageName: String) {
        viewModelScope.launch {
            modifiedAppsRepository.addFavourite(packageName)
        }
    }

    fun removeFavorite(packageName: String) {
        viewModelScope.launch {
            modifiedAppsRepository.removeFavourite(packageName)
        }
    }

    fun reorderFavorite(packageName: String, fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            modifiedAppsRepository.reorderFavouriteApp(packageName, fromIndex, toIndex)
        }
    }
}