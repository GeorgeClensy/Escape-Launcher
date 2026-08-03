package com.geecee.escapelauncher.feature.settings.devoptions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.WeatherSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DevOptionsPageViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val onboardingRepository: OnboardingRepository,
    private val weatherSettingsRepository: WeatherSettingsRepository,
    private val launcherBehaviorRepository: LauncherBehaviorRepository
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
}