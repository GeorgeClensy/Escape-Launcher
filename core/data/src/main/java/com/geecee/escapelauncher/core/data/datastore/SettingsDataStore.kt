package com.geecee.escapelauncher.core.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDatabase
import com.geecee.escapelauncher.core.data.entity.ModifiedAppEntity
import androidx.core.content.edit

private class SharedPreferencesToDataStoreMigration(
    private val context: Context
) : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        // If datastore has any keys, it is being used.
        return currentData.asMap().isEmpty()
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val mutablePrefs = currentData.toMutablePreferences()

        // Check if com.geecee.escapelauncher exists and has entries
        var sharedPrefs = context.getSharedPreferences("com.geecee.escapelauncher", Context.MODE_PRIVATE)
        if (sharedPrefs.all.isEmpty()) {
            // If empty, check 2131755384
            sharedPrefs = context.getSharedPreferences("2131755384", Context.MODE_PRIVATE)
        }

        if (sharedPrefs.all.isEmpty()) {
            return currentData
        }

        Log.d("SettingsDataStore", "Migrating from shared preferences: ${sharedPrefs.all.keys}")

        // Type-specific helpers to migrate settings keys safely without type erasure issues
        fun migrateBoolean(spKey: String, dsKey: Preferences.Key<Boolean>) {
            if (sharedPrefs.contains(spKey)) {
                try {
                    mutablePrefs[dsKey] = sharedPrefs.getBoolean(spKey, false)
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate boolean key $spKey", e)
                }
            }
        }

        fun migrateInt(spKey: String, dsKey: Preferences.Key<Int>) {
            if (sharedPrefs.contains(spKey)) {
                try {
                    mutablePrefs[dsKey] = sharedPrefs.getInt(spKey, 0)
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate int key $spKey", e)
                }
            }
        }

        fun migrateFloat(spKey: String, dsKey: Preferences.Key<Float>) {
            if (sharedPrefs.contains(spKey)) {
                try {
                    mutablePrefs[dsKey] = sharedPrefs.getFloat(spKey, 0f)
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate float key $spKey", e)
                }
            }
        }

        fun migrateString(spKey: String, dsKey: Preferences.Key<String>) {
            if (sharedPrefs.contains(spKey)) {
                try {
                    val value = sharedPrefs.getString(spKey, null)
                    if (value != null) {
                        mutablePrefs[dsKey] = value
                    }
                } catch (e: Exception) {
                    Log.e("SettingsDataStore", "Failed to migrate string key $spKey", e)
                }
            }
        }

        // Migrate all mapped settings
        migrateInt("Theme", PreferencesKeys.THEME)
        migrateBoolean("Analytics", PreferencesKeys.ALLOW_ANALYTICS)
        migrateString("HomeAlignment", PreferencesKeys.HOME_ALIGNMENT)
        migrateString("HomeVAlignment", PreferencesKeys.HOME_V_ALIGNMENT)
        migrateString("AppsAlignment", PreferencesKeys.APPS_ALIGNMENT)
        migrateBoolean("showSearchBox", PreferencesKeys.SHOW_SEARCH_BOX)
        migrateBoolean("searchAutoOpen", PreferencesKeys.SEARCH_AUTO_OPEN)
        migrateBoolean("ShowClock", PreferencesKeys.SHOW_CLOCK)
        migrateBoolean("BigClock", PreferencesKeys.BIG_CLOCK)
        migrateBoolean("screenTimeOnApp", PreferencesKeys.SHOW_SCREEN_TIME_APP)
        migrateBoolean("screenTimeOnHome", PreferencesKeys.SHOW_SCREEN_TIME_HOME)
        migrateString("font", PreferencesKeys.FONT)
        migrateBoolean("FirstTime", PreferencesKeys.FIRST_TIME)
        migrateBoolean("FirstTimeAppDrawHelp", PreferencesKeys.FIRST_TIME_HELP)
        migrateBoolean("searchHiddenPrivateSpace", PreferencesKeys.HIDE_PRIVATE_SPACE)
        migrateBoolean("ShowDate", PreferencesKeys.SHOW_DATE)
        migrateBoolean("TwelveHourClock", PreferencesKeys.TWELVE_HOUR_CLOCK)
        migrateBoolean("HideScreenTimePage", PreferencesKeys.HIDE_SCREEN_TIME_PAGE)
        migrateBoolean("ShowHiddenAppsInSearch", PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH)
        migrateBoolean("bottomSearch", PreferencesKeys.BOTTOM_SEARCH)
        migrateBoolean("AppsListAutoSearch", PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH)
        migrateString("weather_app_package", PreferencesKeys.WEATHER_APP_PACKAGE)
        migrateBoolean("DoubleTapToLock", PreferencesKeys.DOUBLE_TAP_TO_LOCK)
        migrateBoolean("UseFahrenheit", PreferencesKeys.USE_FAHRENHEIT)
        migrateFloat("widget_offset", PreferencesKeys.WIDGET_OFFSET)
        migrateFloat("widget_height", PreferencesKeys.WIDGET_HEIGHT)
        migrateFloat("widget_width", PreferencesKeys.WIDGET_WIDTH)
        migrateInt("widget_ID", PreferencesKeys.WIDGET_ID)
        migrateBoolean("haptic_feedback", PreferencesKeys.HAPTIC_FEEDBACK)
        migrateBoolean("show_weather", PreferencesKeys.SHOW_WEATHER)

        // 3. Migrate app lists to Room database
        try {
            val favoriteAppsJson = sharedPrefs.getString("FavoriteApps", null)
            val favoriteApps = parseJsonStringList(favoriteAppsJson)

            val hiddenAppsJson = sharedPrefs.getString("HiddenApps", null)
            val hiddenApps = parseJsonStringList(hiddenAppsJson)

            val challengeAppsJson = sharedPrefs.getString("ChallengeApps", null)
            val challengeApps = parseJsonStringList(challengeAppsJson)

            if (favoriteApps.isNotEmpty() || hiddenApps.isNotEmpty() || challengeApps.isNotEmpty()) {
                val db = Room.databaseBuilder(
                    context,
                    ModifiedAppsDatabase::class.java,
                    "modified_apps_database"
                ).build()
                val dao = db.modifiedAppsDao()

                val appMap = mutableMapOf<String, ModifiedAppEntity>()

                // Migrate favorite apps
                favoriteApps.forEachIndexed { index, pkg ->
                    appMap[pkg] = ModifiedAppEntity(
                        packageId = pkg,
                        displayName = null,
                        isHidden = false,
                        isChallenge = false,
                        favouritePosition = index.toDouble()
                    )
                }

                // Migrate hidden apps
                hiddenApps.forEach { pkg ->
                    val existing = appMap[pkg]
                    if (existing != null) {
                        appMap[pkg] = existing.copy(isHidden = true)
                    } else {
                        appMap[pkg] = ModifiedAppEntity(
                            packageId = pkg,
                            displayName = null,
                            isHidden = true,
                            isChallenge = false,
                            favouritePosition = null
                        )
                    }
                }

                // Migrate challenge apps
                challengeApps.forEach { pkg ->
                    val existing = appMap[pkg]
                    if (existing != null) {
                        appMap[pkg] = existing.copy(isChallenge = true)
                    } else {
                        appMap[pkg] = ModifiedAppEntity(
                            packageId = pkg,
                            displayName = null,
                            isHidden = false,
                            isChallenge = true,
                            favouritePosition = null
                        )
                    }
                }

                if (appMap.isNotEmpty()) {
                    dao.upsertAll(appMap.values.toList())
                    Log.d("SettingsDataStore", "Migrated ${appMap.size} apps to database")
                }
                db.close()
            }
        } catch (e: Exception) {
            Log.e("SettingsDataStore", "Failed to migrate apps to database", e)
        }

        return mutablePrefs
    }

    override suspend fun cleanUp() {
        try {
            // Delete com.geecee.escapelauncher
            context.getSharedPreferences("com.geecee.escapelauncher", Context.MODE_PRIVATE).edit { clear() }
            context.deleteSharedPreferences("com.geecee.escapelauncher")

            // Delete 2131755384
            context.getSharedPreferences("2131755384", Context.MODE_PRIVATE).edit { clear() }
            context.deleteSharedPreferences("2131755384")
            Log.d("SettingsDataStore", "Cleaned up migrated SharedPreferences files")
        } catch (e: Exception) {
            Log.e("SettingsDataStore", "Failed to cleanup SharedPreferences", e)
        }
    }

    private fun parseJsonStringList(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            json.trim()
                .removePrefix("[")
                .removeSuffix("]")
                .split(",")
                .map { it.trim().removeSurrounding("\"").removeSurrounding("'") }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.e("SettingsDataStore", "Failed to parse json string list: $json", e)
            emptyList()
        }
    }
}

val Context.settingsDataStore by preferencesDataStore(
    name = "settings",
    produceMigrations = { context ->
        listOf(SharedPreferencesToDataStoreMigration(context))
    }
)