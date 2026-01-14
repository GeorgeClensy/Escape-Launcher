@file:Suppress("unused")

package com.geecee.escapelauncher.utils.managers

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.geecee.escapelauncher.utils.managers.Migration

class FavoriteAppsManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = Migration.UNIFIED_PREFS_NAME
        private const val FAVORITE_APPS_KEY = "FavoriteApps"
    }

    private fun saveFavoriteApps(favoriteApps: List<String>) {
        val json = gson.toJson(favoriteApps)
        sharedPreferences.edit {
            putString(FAVORITE_APPS_KEY, json)
        }
    }

    fun getFavoriteApps(): List<String> {
        val json = sharedPreferences.getString(FAVORITE_APPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyList()
        }
    }

    fun addFavoriteApp(packageName: String) {
        val favoriteApps = getFavoriteApps().toMutableList()
        if (packageName !in favoriteApps) {
            favoriteApps.add(packageName)
            saveFavoriteApps(favoriteApps)
        }
    }

    fun removeFavoriteApp(packageName: String) {
        val favoriteApps = getFavoriteApps().toMutableList()
        if (favoriteApps.remove(packageName)) {
            saveFavoriteApps(favoriteApps)
        }
    }

    fun isAppFavorite(packageName: String): Boolean {
        return packageName in getFavoriteApps()
    }

    fun getFavoriteIndex(packageName: String): Int {
        return getFavoriteApps().indexOf(packageName)
    }

    fun reorderFavoriteApps(fromIndex: Int, toIndex: Int) {
        val favoriteApps = getFavoriteApps().toMutableList()
        if (fromIndex in favoriteApps.indices && toIndex in favoriteApps.indices) {
            val item = favoriteApps.removeAt(fromIndex)
            favoriteApps.add(toIndex, item)
            saveFavoriteApps(favoriteApps)
        }
    }

    fun getFavoriteAppsInOrder(): List<String> {
        return getFavoriteApps()
    }
}
