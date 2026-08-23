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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

    private val _navigateHomeEvent = MutableSharedFlow<Unit>(
        replay = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )
    val navigateHomeEvent = _navigateHomeEvent.asSharedFlow()

    fun requestToGoHome() {
        viewModelScope.launch {
            _navigateHomeEvent.emit(Unit)
        }
    }


    val replayOnBoardingEvent = onboardingRepository.replayOnboardingEvent.asSharedFlow()

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
