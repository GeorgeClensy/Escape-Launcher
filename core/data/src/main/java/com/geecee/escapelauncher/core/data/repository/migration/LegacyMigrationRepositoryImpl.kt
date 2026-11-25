package com.geecee.escapelauncher.core.data.repository.migration

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.domain.repository.migration.LegacyMigrationRepository
import com.geecee.escapelauncher.core.model.ModifiedApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegacyMigrationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>
) : LegacyMigrationRepository {

    override suspend fun isLegacyAppsMigrated(): Boolean {
        return dataStore.data.first()[PreferencesKeys.LEGACY_APPS_MIGRATED] ?: false
    }

    override suspend fun setLegacyAppsMigrated(migrated: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.LEGACY_APPS_MIGRATED] = migrated
        }
    }

    override suspend fun readLegacyModifiedApps(): List<ModifiedApp> = withContext(Dispatchers.IO) {
        val favPrefs = context.getSharedPreferences("FavoriteAppsPrefs", Context.MODE_PRIVATE)
        val hiddenPrefs = context.getSharedPreferences("HiddenAppsPrefs", Context.MODE_PRIVATE)
        val challengePrefs = context.getSharedPreferences("ChallengePrefs", Context.MODE_PRIVATE)

        val favoriteApps = parseJsonStringList(favPrefs.getString("FavoriteApps", null))
        val hiddenApps = parseJsonStringList(hiddenPrefs.getString("HiddenApps", null))
        val challengeApps = parseJsonStringList(challengePrefs.getString("ChallengeApps", null))

        val appMap = mutableMapOf<String, ModifiedApp>()

        favoriteApps.forEachIndexed { index, pkg ->
            appMap[pkg] = ModifiedApp(
                packageId = pkg,
                displayName = null,
                isHidden = false,
                isChallenge = false,
                favouritePosition = index.toDouble()
            )
        }

        hiddenApps.forEach { pkg ->
            val existing = appMap[pkg]
            appMap[pkg] = existing?.copy(isHidden = true) ?: ModifiedApp(
                packageId = pkg,
                displayName = null,
                isHidden = true,
                isChallenge = false,
                favouritePosition = null
            )
        }

        challengeApps.forEach { pkg ->
            val existing = appMap[pkg]
            appMap[pkg] = existing?.copy(isChallenge = true) ?: ModifiedApp(
                packageId = pkg,
                displayName = null,
                isHidden = false,
                isChallenge = true,
                favouritePosition = null
            )
        }

        appMap.values.toList()
    }

    override suspend fun clearLegacySharedPreferences() = withContext(Dispatchers.IO) {
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
        val legacyFiles = prefsDir.listFiles()
            ?.filter { it.extension == "xml" }
            ?.map { it.nameWithoutExtension }
            ?.filter { name ->
                val sp = context.getSharedPreferences(name, Context.MODE_PRIVATE)
                name in setOf(
                    "com.geecee.escapelauncher",
                    "widget_prefs",
                    "FavoriteAppsPrefs",
                    "HiddenAppsPrefs",
                    "ChallengePrefs"
                ) || (name.isNotEmpty() && name.all { it.isDigit() }) ||
                        sp.contains("WidgetOffset") ||
                        sp.contains("FirstTime")
            }
            .orEmpty()

        legacyFiles.forEach { name ->
            try {
                context.getSharedPreferences(name, Context.MODE_PRIVATE).edit { clear() }
                context.deleteSharedPreferences(name)
            } catch (e: Exception) {
                Log.e("LegacyMigration", "Failed to cleanup SharedPreferences $name", e)
            }
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
            Log.e("LegacyMigration", "Failed to parse json string list: $json", e)
            emptyList()
        }
    }
}
