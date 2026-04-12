package com.geecee.escapelauncher

import android.content.Context
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.map

@HiltViewModel
class AppsListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SettingsRepository
) : ViewModel() {
    val showScreenTimeApp = repository.showScreenTimeApp

    val appsAlignment = repository.appsAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }
}