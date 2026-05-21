package com.geecee.escapelauncher.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository,
    private val appsRepository: AppsRepository
) : ViewModel() {
    val installedApps = appsRepository.mainUserApps
}
