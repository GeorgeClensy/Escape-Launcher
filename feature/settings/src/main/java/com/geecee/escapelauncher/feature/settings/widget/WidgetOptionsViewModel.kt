package com.geecee.escapelauncher.feature.settings.widget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WidgetOptionsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SettingsRepository,
    val widgetHostManager: WidgetHostManager
) : ViewModel() {
    val widgetOffset = repository.widgetOffset
    fun setWidgetOffset(value: Float) {
        viewModelScope.launch {
            repository.setWidgetOffset(value)
        }
    }

    val widgetHeight = repository.widgetHeight
    fun setWidgetHeight(value: Float) {
        viewModelScope.launch {
            repository.setWidgetHeight(value)
        }
    }

    val widgetWidth = repository.widgetWidth
    fun setWidgetWidth(value: Float) {
        viewModelScope.launch {
            repository.setWidgetWidth(value)
        }
    }

    val widgetId = repository.widgetId

    fun setWidgetId(id: Int) {
        viewModelScope.launch {
            repository.setWidgetId(id)
        }
    }
}
