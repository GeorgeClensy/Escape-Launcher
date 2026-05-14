package com.geecee.escapelauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class GlobalViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {
    val allowAnalytics = repository.allowAnalyitics
    fun setAllowAnalytics(value: Boolean) {
        viewModelScope.launch {
            repository.setAllowAnalytics(value)
        }
    }

    val firstTime = repository.firstTime
    fun setFirstTime(value: Boolean) {
        viewModelScope.launch {
            repository.setFirstTime(value)
        }
    }
}