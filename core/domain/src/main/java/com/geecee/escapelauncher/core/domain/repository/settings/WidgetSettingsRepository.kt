package com.geecee.escapelauncher.core.domain.repository.settings

import kotlinx.coroutines.flow.Flow

interface WidgetSettingsRepository {
    val widgetOffset: Flow<Float>
    suspend fun setWidgetOffset(offset: Float)
    val widgetHeight: Flow<Float>
    suspend fun setWidgetHeight(height: Float)
    val widgetWidth: Flow<Float>
    suspend fun setWidgetWidth(width: Float)
    val widgetId: Flow<Int>
    suspend fun setWidgetId(value: Int)
}
