package com.geecee.escapelauncher.core.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val repository: SettingsRepository
): ViewModel() {
    val theme: StateFlow<AppColourScheme> =
        repository.theme
            .map { id -> AppColourScheme.fromId(id) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AppColourScheme.ESCAPE_THEME
            )

    fun setTheme(scheme: AppColourScheme) {
        viewModelScope.launch {
            repository.setTheme(scheme.id)
        }
    }

    val font = repository.font

    fun setFont(value: String) {
        viewModelScope.launch {
            repository.setFont(value)
        }
    }
}