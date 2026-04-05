package com.geecee.escapelauncher

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
}

@HiltViewModel
class MainSettingsPageViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val twelveHourClock = repository.twelveHourClock
    fun setTwelveHourClock(value: Boolean) {
        viewModelScope.launch {
            repository.setTwelveHourClock(value)
        }
    }
}