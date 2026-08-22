package com.geecee.escapelauncher.feature.onboarding.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val launcherBehaviorRepository: LauncherBehaviorRepository
) : ViewModel() {
    val allowAnalytics = launcherBehaviorRepository.allowAnalyitics

    fun setAllowAnalytics(value: Boolean) {
        viewModelScope.launch {
            launcherBehaviorRepository.setAllowAnalytics(value)
        }
    }
}