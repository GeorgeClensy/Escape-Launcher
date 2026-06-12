package com.geecee.escapelauncher.feature.onboarding.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    fun setAllowAnalytics(value: Boolean) {
        viewModelScope.launch {
            repository.setAllowAnalytics(value)
        }
    }
}