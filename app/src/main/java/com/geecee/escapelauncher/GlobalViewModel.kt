package com.geecee.escapelauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.startup.GetAppInitializationStateUseCase
import com.geecee.escapelauncher.core.model.AppInitializationState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class GlobalViewModel @Inject constructor(
    launcherBehaviorRepository: LauncherBehaviorRepository,
    onboardingRepository: OnboardingRepository,
    appearanceRepository: AppearanceRepository,
    getAppInitializationStateUseCase: GetAppInitializationStateUseCase
) : ViewModel() {
    val allowAnalytics = launcherBehaviorRepository.allowAnalyitics
    val firstTime = onboardingRepository.firstTime
    val showStatusBar = appearanceRepository.showStatusBar
    val showWallpaper = appearanceRepository.showWallpaper

    /**
     * The initialization state for the splash screen.
     * Combines multiple data loading signals to determine when the app is ready for interaction.
     */
    val initializationState = getAppInitializationStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppInitializationState()
        )
}
