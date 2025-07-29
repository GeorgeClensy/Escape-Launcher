package com.geecee.escapelauncher.utils.managers

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HiddenAppsManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "HiddenAppsPrefs"
        private const val FAVORITE_APPS_KEY = "HiddenApps"
    }

    private fun saveHiddenApps(hiddenApps: List<String>) {
        val json = gson.toJson(hiddenApps)
        sharedPreferences.edit {
            putString(FAVORITE_APPS_KEY, json)
        }
    }

    fun getHiddenApps(): List<String> {
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addHiddenApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableList()
        if (packageName !in hiddenApps) {
            hiddenApps.add(packageName)
            saveHiddenApps(hiddenApps)
        }
    }

    fun removeHiddenApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableList()
        if (hiddenApps.remove(packageName)) {
            saveHiddenApps(hiddenApps)
        }
    }

    fun isAppHidden(packageName: String): Boolean {
        val hiddenApps = getHiddenApps()
        return packageName in hiddenApps
    }
}

