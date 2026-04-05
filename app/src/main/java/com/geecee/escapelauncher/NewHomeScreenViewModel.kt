package com.geecee.escapelauncher

import android.content.Context
import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

@HiltViewModel
class NewHomeScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val twelveHourClock = repository.twelveHourClock

}