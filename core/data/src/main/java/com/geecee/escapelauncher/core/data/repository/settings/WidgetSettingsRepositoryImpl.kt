package com.geecee.escapelauncher.core.data.repository.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.settings.WidgetSettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WidgetSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : WidgetSettingsRepository {
    override val widgetOffset: Flow<Float> = dataStore.data.map { it[PreferencesKeys.WIDGET_OFFSET] ?: DefaultSettings.WIDGET_OFFSET }
    override suspend fun setWidgetOffset(offset: Float) {
        dataStore.edit { it[PreferencesKeys.WIDGET_OFFSET] = offset }
    }
    override val widgetHeight: Flow<Float> = dataStore.data.map { it[PreferencesKeys.WIDGET_HEIGHT] ?: DefaultSettings.WIDGET_HEIGHT }
    override suspend fun setWidgetHeight(height: Float) {
        dataStore.edit { it[PreferencesKeys.WIDGET_HEIGHT] = height }
    }
    override val widgetWidth: Flow<Float> = dataStore.data.map { it[PreferencesKeys.WIDGET_WIDTH] ?: DefaultSettings.WIDGET_WIDTH }
    override suspend fun setWidgetWidth(width: Float) {
        dataStore.edit { it[PreferencesKeys.WIDGET_WIDTH] = width }
    }
    override val widgetId: Flow<Int> = dataStore.data.map { it[PreferencesKeys.WIDGET_ID] ?: DefaultSettings.WIDGET_ID }
    override suspend fun setWidgetId(value: Int) {
        dataStore.edit { it[PreferencesKeys.WIDGET_ID] = value }
    }
}
