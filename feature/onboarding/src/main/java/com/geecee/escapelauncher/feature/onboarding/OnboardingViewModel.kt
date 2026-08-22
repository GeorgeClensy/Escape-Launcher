package com.geecee.escapelauncher.feature.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.launcher.GetIsDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.launcher.SetDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.onboarding.GetOnboardingScreenUseCase
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
    private val setDefaultLauncherUseCase: SetDefaultLauncherUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val screens = getOnboardingScreensUseCase()

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    fun updateLauncherStatus() {
        _isDefaultLauncher.value = getIsDefaultLauncherUseCase()
    }

    fun getPromptDefaultLauncherIntent(): android.content.Intent {
        return setDefaultLauncherUseCase()
    }

    init {
        updateLauncherStatus()
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            onboardingRepository.setFirstTime(false)
        }
    }

    val currentPageIndex: StateFlow<Int> = savedStateHandle.getStateFlow(KEY_CURRENT_PAGE_INDEX, 0)

    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

    fun onPageSettled(pageIndex: Int) {
        savedStateHandle[KEY_CURRENT_PAGE_INDEX] = pageIndex
    }

    companion object {
        private const val KEY_CURRENT_PAGE_INDEX = "current_page_index"
    }
}
