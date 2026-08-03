package com.geecee.escapelauncher

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GlobalViewModel @Inject constructor(
    val modifiedAppsRepository: ModifiedAppsRepository,
    val appsRepository: AppsRepository,
    private val launcherBehaviorRepository: LauncherBehaviorRepository,
    private val onboardingRepository: OnboardingRepository,
    private val appearanceRepository: AppearanceRepository
) : ViewModel() {
    val allowAnalytics = launcherBehaviorRepository.allowAnalyitics
    val firstTime = onboardingRepository.firstTime
    val showStatusBar = appearanceRepository.showStatusBar

    private val _navigateHomeEvent = MutableSharedFlow<Unit>(
        replay = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )
    val navigateHomeEvent = _navigateHomeEvent.asSharedFlow()

    fun requestToGoHome() {
        viewModelScope.launch {
            _navigateHomeEvent.emit(Unit)
        }
    }

    // Loading states for splash screen
    val isAppsLoaded = mutableStateOf(false)
    val isFavoritesLoaded = mutableStateOf(false)
    val isSettingsLoaded = mutableStateOf(false)

    init {
        // Keep isAppsLoaded in sync with repository
        viewModelScope.launch {
            appsRepository.installedApps.collect {
                if (it.isNotEmpty()) {
                    isAppsLoaded.value = true
                }
            }
        }

        // Keep isFavoritesLoaded in sync
        viewModelScope.launch {
            modifiedAppsRepository.getFavouriteAppsInOrderFlow().collect {
                isFavoritesLoaded.value = true
            }
        }

        // Keep isSettingsLoaded in sync
        viewModelScope.launch {
            showStatusBar.collect {
                isSettingsLoaded.value = true
            }
        }
    }
}
