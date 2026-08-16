package com.geecee.escapelauncher.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.onboarding.GetOnboardingScreenUseCase
import com.geecee.escapelauncher.core.domain.onboarding.OnboardingScreen
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    getOnboardingScreensUseCase: GetOnboardingScreenUseCase
) : ViewModel() {
    val screens = getOnboardingScreensUseCase()

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
