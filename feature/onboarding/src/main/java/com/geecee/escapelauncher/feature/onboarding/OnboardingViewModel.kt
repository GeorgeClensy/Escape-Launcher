package com.geecee.escapelauncher.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingScreen {
    WELCOME,
    STATISTICS,
    FAVORITES,
    DEFAULT_LAUNCHER,
    ANALYTICS,
    ACCESSIBILITY
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    appConfiguration: AppConfiguration,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val isFoss = appConfiguration.isFoss
    val showAccessibility = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P

    val screens = listOfNotNull(
        OnboardingScreen.WELCOME,
        OnboardingScreen.STATISTICS,
        OnboardingScreen.FAVORITES,
        OnboardingScreen.DEFAULT_LAUNCHER,
        if (!isFoss) OnboardingScreen.ANALYTICS else null,
        if (showAccessibility) OnboardingScreen.ACCESSIBILITY else null
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setFirstTime(false)
        }
    }

    val startFromLauncherPage = settingsRepository.isOnDefaultLauncherOnboarding

    fun setStartFromLauncherPage(value: Boolean) {
        viewModelScope.launch {
            settingsRepository.setOnDefaultLauncherOnboarding(value)
        }
    }
}
