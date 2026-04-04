package com.geecee.escapelauncher.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    override val hapticFeedBackEnabled: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true
        }

    override suspend fun setHapticFeedback(enabld: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabld }
    }

    override val hidePrivateSpace: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HIDE_PRIVATE_SPACE] ?: true
        }

    override suspend fun setHidePrivateSpace(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HIDE_PRIVATE_SPACE] = enabled }
    }
}