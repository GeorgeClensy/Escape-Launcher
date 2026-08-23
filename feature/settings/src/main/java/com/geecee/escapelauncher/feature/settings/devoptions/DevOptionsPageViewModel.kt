package com.geecee.escapelauncher.feature.settings.devoptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.WeatherSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.system.LockScreenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DevOptionsPageViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    private val weatherSettingsRepository: WeatherSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    private val lockScreenUseCase: LockScreenUseCase
) : ViewModel() {
    val firstTimeHelp = onboardingRepository.firstTimeHelp
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            onboardingRepository.setFirstTimeHelp(value)
        }
    }

    val firstTime = onboardingRepository.firstTime
    fun setFirstTime(value: Boolean) {
        viewModelScope.launch {
            onboardingRepository.setFirstTime(value)
        }
    }

    fun setWeatherAppPackage(value: String) {
        viewModelScope.launch {
            weatherSettingsRepository.setWeatherAppPackage(value)
        }
    }

    val doubleTapToLock = launcherBehaviorRepository.doubleTapToLock

    fun lockScreen() {
        lockScreenUseCase()
    }

    fun requestReplayOnboarding() {
        viewModelScope.launch {
            onboardingRepository.replayOnboardingEvent.emit(Unit)
        }
    }
}