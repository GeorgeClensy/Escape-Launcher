package com.geecee.escapelauncher.utils.managers

import android.content.Context
import android.os.Build
import android.util.Log

class Migration(private val context: Context) {

    companion object {
        private const val TAG = "Migration"
        const val UNIFIED_PREFS_NAME = "com.geecee.escapelauncher"
        private const val MIGRATION_COMPLETE_KEY = "MigrationComplete"

        private val OLD_PREFS_FILES = listOf(
            "FavoriteAppsPrefs", "HiddenAppsPrefs", "ChallengePrefs", "SettingsPref"
        )
    }

    /**
     * Orchestrates the migration process.
     * Call this from your Application class or MainActivity.
     */
    fun migrateToUnifiedPrefs() {
        val unifiedPrefs = context.getSharedPreferences(UNIFIED_PREFS_NAME, Context.MODE_PRIVATE)

        if (unifiedPrefs.getBoolean(MIGRATION_COMPLETE_KEY, false)) {
            Log.d(TAG, "Migration already completed previously.")
            return
        }

        val editor = unifiedPrefs.edit()
        var changesMade = false

        OLD_PREFS_FILES.forEach { oldName ->
            // Check if the file actually exists on the disk
            val prefsFile = java.io.File(context.applicationInfo.dataDir, "shared_prefs/$oldName.xml")

            if (prefsFile.exists()) {
                val oldPrefs = context.getSharedPreferences(oldName, Context.MODE_PRIVATE)
                val allEntries = oldPrefs.all

                if (allEntries.isNotEmpty()) {
                    Log.d(TAG, "Migrating $oldName (${allEntries.size} keys)")
                    changesMade = true

                    for ((key, value) in allEntries) {
                        when (value) {
                            is Boolean -> editor.putBoolean(key, value)
                            is Float -> editor.putFloat(key, value)
                            is Int -> editor.putInt(key, value)
                            is Long -> editor.putLong(key, value)
                            is String -> editor.putString(key, value)
                            is Set<*> -> {
                                @Suppress("UNCHECKED_CAST")
                                editor.putStringSet(key, value as? Set<String>)
                            }
                        }
                    }

                    // Clear and delete
                    oldPrefs.edit().clear().apply()
                    deleteOldPrefsFile(oldName)
                }
            } else {
                Log.d(TAG, "Skipping $oldName: File does not exist.")
            }
        }

        // Only apply and mark complete if we finished the loop successfully
        editor.putBoolean(MIGRATION_COMPLETE_KEY, true)
        editor.apply()

        if (changesMade) {
            Log.d(TAG, "All existing preferences migrated to $UNIFIED_PREFS_NAME")
        }
    }

    private fun deleteOldPrefsFile(name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.deleteSharedPreferences(name)
        } else {
            // Manual deletion for older versions
            try {
                val dir = java.io.File(context.applicationInfo.dataDir, "shared_prefs")
                val file = java.io.File(dir, "$name.xml")
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete old prefs file: $name", e)
            }
        }
    }
}