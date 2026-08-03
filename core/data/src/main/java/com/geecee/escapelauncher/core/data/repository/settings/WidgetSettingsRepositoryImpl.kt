package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.WidgetSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WidgetSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : WidgetSettingsRepository {
    override val widgetOffset: Flow<Float> = dataStore.data.map { it[PreferencesKeys.WIDGET_OFFSET] ?: 0f }
    override suspend fun setWidgetOffset(offset: Float) {
        dataStore.edit { it[PreferencesKeys.WIDGET_OFFSET] = offset }
    }
    override val widgetHeight: Flow<Float> = dataStore.data.map { it[PreferencesKeys.WIDGET_HEIGHT] ?: 125f }
    override suspend fun setWidgetHeight(height: Float) {
        dataStore.edit { it[PreferencesKeys.WIDGET_HEIGHT] = height }
    }
    override val widgetWidth: Flow<Float> = dataStore.data.map { it[PreferencesKeys.WIDGET_WIDTH] ?: 250f }
    override suspend fun setWidgetWidth(width: Float) {
        dataStore.edit { it[PreferencesKeys.WIDGET_WIDTH] = width }
    }
    override val widgetId: Flow<Int> = dataStore.data.map { it[PreferencesKeys.WIDGET_ID] ?: -1 }
    override suspend fun setWidgetId(value: Int) {
        dataStore.edit { it[PreferencesKeys.WIDGET_ID] = value }
    }
}
