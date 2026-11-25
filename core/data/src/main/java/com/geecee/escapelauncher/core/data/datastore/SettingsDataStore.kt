package com.geecee.escapelauncher.core.data.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.File

private class SharedPreferencesToDataStoreMigration(
    private val context: Context
) : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        // If datastore has any keys, it is being used.
        return currentData.asMap().isEmpty()
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        val allPrefNames = prefsDir.listFiles()
            ?.filter { it.extension == "xml" }
            ?.map { it.nameWithoutExtension }
            .orEmpty()

        val prefsList = allPrefNames
            .map { context.getSharedPreferences(it, Context.MODE_PRIVATE) }
            .filter { it.all.isNotEmpty() }

        if (prefsList.isEmpty()) {
            return currentData
        }

        val mutablePrefs = currentData.toMutablePreferences()

        fun migrateBoolean(sp: SharedPreferences, spKey: String, dsKey: Preferences.Key<Boolean>) {
            if (sp.contains(spKey)) {
                try {
                    mutablePrefs[dsKey] = sp.getBoolean(spKey, false)
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate boolean key $spKey", e)
                }
            }
        }

        fun migrateInt(sp: SharedPreferences, spKey: String, dsKey: Preferences.Key<Int>) {
            if (sp.contains(spKey)) {
                try {
                    mutablePrefs[dsKey] = sp.getInt(spKey, 0)
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate int key $spKey", e)
                }
            }
        }

        fun migrateFloat(sp: SharedPreferences, spKey: String, dsKey: Preferences.Key<Float>) {
            if (sp.contains(spKey)) {
                try {
                    mutablePrefs[dsKey] = sp.getFloat(spKey, 0f)
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate float key $spKey", e)
                }
            }
        }

        fun migrateString(sp: SharedPreferences, spKey: String, dsKey: Preferences.Key<String>) {
            if (sp.contains(spKey)) {
                try {
                    val value = sp.getString(spKey, null)
                    if (value != null) {
                        mutablePrefs[dsKey] = value
                    }
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate string key $spKey", e)
                }
            }
        }

        // Track if any of our own legacy prefs actually existed (vs third-party GMS/Firebase XMLs)
        var foundLegacyLauncherPrefs = false

        for (sp in prefsList) {
            migrateBoolean(sp, "Analytics", PreferencesKeys.ALLOW_ANALYTICS)
            migrateString(sp, "HomeAlignment", PreferencesKeys.HOME_ALIGNMENT)
            migrateString(sp, "HomeVAlignment", PreferencesKeys.HOME_V_ALIGNMENT)
            migrateString(sp, "AppsAlignment", PreferencesKeys.APPS_ALIGNMENT)
            migrateBoolean(sp, "showSearchBox", PreferencesKeys.SHOW_SEARCH_BOX)
            migrateBoolean(sp, "searchAutoOpen", PreferencesKeys.SEARCH_AUTO_OPEN)
            migrateBoolean(sp, "ShowClock", PreferencesKeys.SHOW_CLOCK)
            migrateBoolean(sp, "BigClock", PreferencesKeys.BIG_CLOCK)
            migrateBoolean(sp, "screenTimeOnApp", PreferencesKeys.SHOW_SCREEN_TIME_APP)
            migrateBoolean(sp, "screenTimeOnHome", PreferencesKeys.SHOW_SCREEN_TIME_HOME)
            migrateString(sp, "font", PreferencesKeys.FONT)
            migrateBoolean(sp, "FirstTime", PreferencesKeys.FIRST_TIME)
            migrateBoolean(sp, "FirstTimeAppDrawHelp", PreferencesKeys.FIRST_TIME_HELP)
            migrateBoolean(sp, "searchHiddenPrivateSpace", PreferencesKeys.HIDE_PRIVATE_SPACE)
            migrateBoolean(sp, "ShowDate", PreferencesKeys.SHOW_DATE)
            migrateBoolean(sp, "TwelveHourClock", PreferencesKeys.TWELVE_HOUR_CLOCK)
            migrateBoolean(sp, "HideScreenTimePage", PreferencesKeys.HIDE_SCREEN_TIME_PAGE)
            migrateBoolean(sp, "ShowHiddenAppsInSearch", PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH)
            migrateBoolean(sp, "AppsListAutoSearch", PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH)
            migrateString(sp, "weather_app_package", PreferencesKeys.WEATHER_APP_PACKAGE)
            migrateBoolean(sp, "DoubleTapToLock", PreferencesKeys.DOUBLE_TAP_TO_LOCK)
            migrateBoolean(sp, "UseFahrenheit", PreferencesKeys.USE_FAHRENHEIT)
            migrateBoolean(sp, "haptic_feedback", PreferencesKeys.HAPTIC_FEEDBACK)
            migrateBoolean(sp, "show_weather", PreferencesKeys.SHOW_WEATHER)

            // Widget settings (old Widgets.kt keys + newer snake_case keys)
            migrateFloat(sp, "WidgetOffset", PreferencesKeys.WIDGET_OFFSET)
            migrateFloat(sp, "widget_offset", PreferencesKeys.WIDGET_OFFSET)
            migrateFloat(sp, "WidgetHeight", PreferencesKeys.WIDGET_HEIGHT)
            migrateFloat(sp, "widget_height", PreferencesKeys.WIDGET_HEIGHT)
            migrateFloat(sp, "WidgetWidth", PreferencesKeys.WIDGET_WIDTH)
            migrateFloat(sp, "widget_width", PreferencesKeys.WIDGET_WIDTH)
            migrateInt(sp, "widget_id", PreferencesKeys.WIDGET_ID)
            migrateInt(sp, "widget_ID", PreferencesKeys.WIDGET_ID)

            // Theme mapping
            migrateOldTheme(sp, mutablePrefs)

            if (sp.contains("FirstTime") || sp.contains("HomeAlignment") || sp.contains("WidgetOffset") ||
                sp.contains("widget_id") || sp.contains("FavoriteApps") || sp.contains("Theme") || sp.contains("theme")
            ) {
                foundLegacyLauncherPrefs = true
            }
        }

        // Preserve old app defaults for upgrading users who left those settings untouched
        if (foundLegacyLauncherPrefs) {
            if (PreferencesKeys.HOME_ALIGNMENT !in mutablePrefs) mutablePrefs[PreferencesKeys.HOME_ALIGNMENT] = "Center"
            if (PreferencesKeys.APPS_ALIGNMENT !in mutablePrefs) mutablePrefs[PreferencesKeys.APPS_ALIGNMENT] = "Center"
            if (PreferencesKeys.FONT !in mutablePrefs) mutablePrefs[PreferencesKeys.FONT] = "Jost"
            if (PreferencesKeys.SHOW_DATE !in mutablePrefs) mutablePrefs[PreferencesKeys.SHOW_DATE] = false
            if (PreferencesKeys.SHOW_WEATHER !in mutablePrefs) mutablePrefs[PreferencesKeys.SHOW_WEATHER] = false
            if (PreferencesKeys.SHOW_SCREEN_TIME_HOME !in mutablePrefs) mutablePrefs[PreferencesKeys.SHOW_SCREEN_TIME_HOME] = false
            if (PreferencesKeys.SHOW_SCREEN_TIME_APP !in mutablePrefs) mutablePrefs[PreferencesKeys.SHOW_SCREEN_TIME_APP] = false
            if (PreferencesKeys.DOUBLE_TAP_TO_LOCK !in mutablePrefs) mutablePrefs[PreferencesKeys.DOUBLE_TAP_TO_LOCK] = false
            if (PreferencesKeys.WIDGET_WIDTH !in mutablePrefs) mutablePrefs[PreferencesKeys.WIDGET_WIDTH] = 150f

            // Old default theme was 11 (OFF_LIGHT)
            if (PreferencesKeys.THEME !in mutablePrefs) {
                mutablePrefs[PreferencesKeys.THEME] = 11 // ORANGE
                mutablePrefs[PreferencesKeys.NIGHT_MODE] = 2 // Light
            }
        }

        return mutablePrefs
    }

    private fun migrateOldTheme(
        sp: SharedPreferences,
        mutablePrefs: MutablePreferences
    ) {
        val autoSwitch = sp.getBoolean("autoThemeSwitch", false)
        val oldThemeId = when {
            autoSwitch -> {
                val d = sp.getInt("dTheme", -1)
                val l = sp.getInt("lTheme", -1)
                if (d != -1) d else l
            }
            sp.contains("Theme") -> sp.getInt("Theme", -1)
            sp.contains("theme") -> sp.getInt("theme", -1)
            else -> -1
        }

        if (oldThemeId == -1) return

        when (oldThemeId) {
            0 -> { // Old DARK
                mutablePrefs[PreferencesKeys.THEME] = 0 // MONOCHROME
                mutablePrefs[PreferencesKeys.NIGHT_MODE] = if (autoSwitch) 0 else 1
            }
            1 -> { // Old LIGHT
                mutablePrefs[PreferencesKeys.THEME] = 0 // MONOCHROME
                mutablePrefs[PreferencesKeys.NIGHT_MODE] = if (autoSwitch) 0 else 2
            }
            2 -> { // Old PITCH_DARK
                mutablePrefs[PreferencesKeys.THEME] = 0 // MONOCHROME
                mutablePrefs[PreferencesKeys.BLACK_BACKGROUND] = true
                mutablePrefs[PreferencesKeys.NIGHT_MODE] = 1
            }
            3, 5, 7, 9, 11 -> { // Old LIGHT_RED, LIGHT_GREEN, LIGHT_BLUE, LIGHT_YELLOW, OFF_LIGHT
                mutablePrefs[PreferencesKeys.THEME] = oldThemeId
                mutablePrefs[PreferencesKeys.NIGHT_MODE] = if (autoSwitch) 0 else 2
            }
            4, 6, 8, 10 -> { // Old DARK_RED, DARK_GREEN, DARK_BLUE, DARK_YELLOW
                mutablePrefs[PreferencesKeys.THEME] = oldThemeId - 1
                mutablePrefs[PreferencesKeys.NIGHT_MODE] = if (autoSwitch) 0 else 1
            }
            12 -> { // Old SYSTEM
                mutablePrefs[PreferencesKeys.THEME] = 12
                mutablePrefs[PreferencesKeys.NIGHT_MODE] = 0
            }
        }
    }

    override suspend fun cleanUp() = Unit
}

val Context.settingsDataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesToDataStoreMigration(context))
    }
)