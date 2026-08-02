package com.geecee.escapelauncher.feature.onboarding.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.apps.GetFavoriteAppsUseCase
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    getFavoriteAppsUseCase: GetFavoriteAppsUseCase
) : ViewModel() {
    val installedApps = appsRepository.mainUserApps

    val favoriteApps: StateFlow<List<InstalledApp>> = getFavoriteAppsUseCase()
        .stateIn(
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