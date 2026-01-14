package com.geecee.escapelauncher.utils.managers

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HiddenAppsManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = Migration.UNIFIED_PREFS_NAME
        private const val FAVORITE_APPS_KEY = "HiddenApps"
    }

    private var cache: List<String>? = null

    private fun saveHiddenApps(hiddenApps: List<String>) {
        cache = hiddenApps
        val json = gson.toJson(hiddenApps)
        sharedPreferences.edit {
            putString(FAVORITE_APPS_KEY, json)
        }
    }

    fun getHiddenApps(): List<String> {
        if (cache != null) {
            return cache!!
        }
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        cache = if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
        return cache!!
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
        return packageName in getHiddenApps()
    }
}

