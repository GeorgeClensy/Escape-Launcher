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

    override val showStatusBar: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_STATUS_BAR] ?: false
        }

    override suspend fun setShowStatusBar(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_STATUS_BAR] = enabled }
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

    override val useFahrenheit: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.USE_FAHRENHEIT] ?: false
        }

    override suspend fun setUseFahrenheit(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.USE_FAHRENHEIT] = enabled }
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

    override val widgetOffset: Flow<Float>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WIDGET_OFFSET] ?: 0f
        }

    override suspend fun setWidgetOffset(offset: Float) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_OFFSET] = offset }
    }

    override val widgetHeight: Flow<Float>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WIDGET_HEIGHT] ?: 125f
        }

    override suspend fun setWidgetHeight(height: Float) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_HEIGHT] = height }
    }

    override val widgetWidth: Flow<Float>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WIDGET_WIDTH] ?: 250f
        }

    override suspend fun setWidgetWidth(width: Float) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_WIDTH] = width }
    }

    override val weatherAppPackage: Flow<String>
        get() = dataStore.data.map {  preferences ->
            preferences[PreferencesKeys.WEATHER_APP_PACKAGE] ?: ""
        }

    override suspend fun setWeatherAppPackage(value: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WEATHER_APP_PACKAGE] = value }
    }

    override val appsAlignment: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.APPS_ALIGNMENT] ?: "Center"
        }

    override suspend fun setAppsAlignment(alignment: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.APPS_ALIGNMENT] = alignment }
    }

    override val theme: Flow<Int>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.THEME] ?: 11
        }

    override suspend fun setTheme(theme: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.THEME] = theme }
    }

    override val ltheme: Flow<Int>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LTHEME] ?: 11
        }

    override suspend fun setLTheme(theme: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.LTHEME] = theme }
    }

    override val dtheme: Flow<Int>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DTHEME] ?: 11
        }

    override suspend fun setDTheme(theme: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.DTHEME] = theme }
    }

    override val syncTheme: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SYNC_THEME] ?: false
        }

    override suspend fun setSyncTheme(value: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SYNC_THEME] = value }
    }

    override val font: Flow<String>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.FONT] ?: "Jost"
        }

    override suspend fun setFont(value: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FONT] = value }
    }

    override val allowAnalyitics: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.ALLOW_ANALYTICS] ?: false
        }

    override suspend fun setAllowAnalytics(value: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.ALLOW_ANALYTICS] = value }
    }

    override val doubleTapToLock: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.DOUBLE_TAP_TO_LOCK] ?: false
        }

    override suspend fun setDoubleTapToLock(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.DOUBLE_TAP_TO_LOCK] = enabled }
    }

    override val showSearchBox: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_SEARCH_BOX] ?: true
        }

    override suspend fun setShowSearchBox(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_SEARCH_BOX] = enabled }
    }

    override val searchAutoOpen: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SEARCH_AUTO_OPEN] ?: false
        }

    override suspend fun setSearchAutoOpen(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SEARCH_AUTO_OPEN] = enabled }
    }

    override val bottomSearch: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.BOTTOM_SEARCH] ?: false
        }

    override suspend fun setBottomSearch(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BOTTOM_SEARCH] = enabled }
    }

    override val automaticallyOpenAppsInSearch: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH] ?: false
        }

    override suspend fun setAutomaticallyOpenAppsInSearch(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH] = enabled }
    }

    override val hideScreenTimePage: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.HIDE_SCREEN_TIME_PAGE] ?: false
        }

    override suspend fun setHideScreenTimePage(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HIDE_SCREEN_TIME_PAGE] = enabled }
    }

    override val showHiddenAppsInSearch: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: false
        }

    override suspend fun setShowHiddenAppsInSearch(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH] = enabled }
    }

    override val firstTime: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.FIRST_TIME] ?: true
        }

    override suspend fun setFirstTime(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FIRST_TIME] = enabled }
    }

    override val widgetId: Flow<Int>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WIDGET_ID] ?: -1
        }

    override suspend fun setWidgetId(value: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_ID] = value }
    }

    override val isOnDefaultLauncherOnboarding: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE] ?: false
        }

    override suspend fun setOnDefaultLauncherOnboarding(value: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE] = value }
    }
}
