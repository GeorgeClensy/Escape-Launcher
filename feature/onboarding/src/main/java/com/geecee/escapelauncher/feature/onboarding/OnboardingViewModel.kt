package com.geecee.escapelauncher.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.launcher.GetIsDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.launcher.SetDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.onboarding.GetOnboardingScreenUseCase
import com.geecee.escapelauncher.core.domain.onboarding.OnboardingScreen
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingRepository: OnboardingRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    getOnboardingScreensUseCase: GetOnboardingScreenUseCase,
    private val getIsDefaultLauncherUseCase: GetIsDefaultLauncherUseCase,
    private val setDefaultLauncherUseCase: SetDefaultLauncherUseCase
) : ViewModel() {
    val screens = getOnboardingScreensUseCase()

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    fun updateLauncherStatus() {
        _isDefaultLauncher.value = getIsDefaultLauncherUseCase()
    }

    fun promptSetDefaultLauncher() {
        setDefaultLauncherUseCase()
    }

    init {
        updateLauncherStatus()
    }

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
