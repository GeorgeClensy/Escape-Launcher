package com.geecee.escapelauncher.feature.settings.devoptions

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class DevOptionsPageViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val firstTimeHelp = repository.firstTimeHelp
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            repository.setFirstTimeHelp(value)
        }
    }

    val firstTime = repository.firstTime
    fun setFirstTime(value: Boolean) {
        viewModelScope.launch {
            repository.setFirstTime(value)
        }
    }

    fun setWeatherAppPackage(value: String) {
        viewModelScope.launch {
            repository.setWeatherAppPackage(value)
        }
    }

    val doubleTapToLock = repository.doubleTapToLock
}