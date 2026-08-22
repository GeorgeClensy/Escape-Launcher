package com.geecee.escapelauncher.feature.settings.widget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.settings.WidgetSettingsRepository
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WidgetOptionsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val widgetSettingsRepository: WidgetSettingsRepository,
    val widgetHostManager: WidgetHostManager
) : ViewModel() {
    val widgetOffset = widgetSettingsRepository.widgetOffset
    fun setWidgetOffset(value: Float) {
        viewModelScope.launch {
            widgetSettingsRepository.setWidgetOffset(value)
        }
    }

    val widgetHeight = widgetSettingsRepository.widgetHeight
    fun setWidgetHeight(value: Float) {
        viewModelScope.launch {
            widgetSettingsRepository.setWidgetHeight(value)
        }
    }

    val widgetWidth = widgetSettingsRepository.widgetWidth
    fun setWidgetWidth(value: Float) {
        viewModelScope.launch {
            widgetSettingsRepository.setWidgetWidth(value)
        }
    }

    val widgetId = widgetSettingsRepository.widgetId

    fun setWidgetId(id: Int) {
        viewModelScope.launch {
            widgetSettingsRepository.setWidgetId(id)
        }
    }
}
