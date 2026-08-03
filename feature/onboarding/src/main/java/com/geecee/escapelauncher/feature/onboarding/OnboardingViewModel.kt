package com.geecee.escapelauncher.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OnboardingScreen {
    WELCOME,
    STATISTICS,
    FAVORITES,
    DEFAULT_LAUNCHER,
    ANALYTICS,
    ACCESSIBILITY,
    FINISHED
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    appConfiguration: AppConfiguration,
    private val onboardingRepository: OnboardingRepository,
    private val launcherBehaviorRepository: LauncherBehaviorRepository
) : ViewModel() {
    val isFoss = appConfiguration.isFoss
    val showAccessibility = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P

    val screens = listOfNotNull(
        OnboardingScreen.WELCOME,
        OnboardingScreen.STATISTICS,
        OnboardingScreen.FAVORITES,
        OnboardingScreen.DEFAULT_LAUNCHER,
        if (!isFoss) OnboardingScreen.ANALYTICS else null,
        if (showAccessibility) OnboardingScreen.ACCESSIBILITY else null,
        OnboardingScreen.FINISHED
    )

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setFirstTime(false)
        }
    }

    val startFromLauncherPage = onboardingRepository.isOnDefaultLauncherOnboarding

    fun setStartFromLauncherPage(value: Boolean) {
        viewModelScope.launch {
            onboardingRepository.setOnDefaultLauncherOnboarding(value)
        }
    }

    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

    fun onPageSettled(pageIndex: Int) {
        val launcherPageIndex = screens.indexOf(OnboardingScreen.DEFAULT_LAUNCHER)
        if (pageIndex != launcherPageIndex) {
            setStartFromLauncherPage(false)
        } else {
            setStartFromLauncherPage(true)
        }
    }
}
