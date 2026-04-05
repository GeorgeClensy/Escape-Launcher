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

    //Private Space
    override val hidePrivateSpace: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HIDE_PRIVATE_SPACE] ?: true
        }

    override suspend fun setHidePrivateSpace(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HIDE_PRIVATE_SPACE] = enabled }
    }

    //Home Screen
    override val twelveHourClock: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.TWELVE_HOUR_CLOCK] ?: false
        }

    override suspend fun setTwelveHourClock(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.TWELVE_HOUR_CLOCK] = enabled }
    }

    override val showClock: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_CLOCK] ?: true
        }

    override suspend fun setShowClock(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_CLOCK] = enabled }
    }

    override val bigClock: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.BIG_CLOCK] ?: false
        }

    override suspend fun setBigClock(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BIG_CLOCK] = enabled }
    }

    override val showDate: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_DATE] ?: false
        }

    override suspend fun setShowDate(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_DATE] = enabled }
    }

    override val showScreenTimeHome: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_SCREEN_TIME_HOME] ?: false
        }

    override suspend fun setShowScreenTimeHome(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_SCREEN_TIME_HOME] = enabled }
    }

    override val showWeather: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_WEATHER] ?: false
        }

    override suspend fun setShowWeather(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_WEATHER] = enabled }
    }

    override val showScreenTimeApp: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_SCREEN_TIME_APP] ?: false
        }

    override suspend fun setShowScreenTimeApp(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_SCREEN_TIME_APP] = enabled }
    }

    override val firstTimeHelp: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.FIRST_TIME_HELP] ?: true
        }

    override suspend fun setFirstTimeHelp(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FIRST_TIME_HELP] = enabled }
    }

    override val homeVAlignment: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HOME_V_ALIGNMENT] ?: "Center"
        }

    override suspend fun setHomeVAlignment(alignment: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HOME_V_ALIGNMENT] = alignment }
    }

    override val homeAlignment: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HOME_ALIGNMENT] ?: "Center"
        }

    override suspend fun setHomeAlignment(alignment: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HOME_ALIGNMENT] = alignment }
    }
}