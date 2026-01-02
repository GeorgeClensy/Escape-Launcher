package com.geecee.escapelauncher.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.util.Log

data class AppShortcut(
    val id: String,
    val label: String,
    val rank: Int
)

fun getAppShortcuts(context: Context, packageName: String): List<AppShortcut> {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    val query = LauncherApps.ShortcutQuery().apply {
        setPackage(packageName)
        setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or 
                     LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or 
                     LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
    }

    return try {
        launcherApps.getShortcuts(query, Process.myUserHandle())
            ?.sortedBy { it.rank }
            ?.map { AppShortcut(it.id, it.shortLabel?.toString() ?: "", it.rank) }
            ?: emptyList()
    } catch (e: SecurityException) {
        Log.e("ShortcutUtils", "SecurityException while getting shortcuts", e)
        emptyList()
    } catch (e: Exception) {
        Log.e("ShortcutUtils", "Error getting shortcuts", e)
        emptyList()
    }
}

fun startShortcut(context: Context, packageName: String, shortcutId: String) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    try {
        launcherApps.startShortcut(packageName, shortcutId, null, null, Process.myUserHandle())
    } catch (e: Exception) {
        Log.e("ShortcutUtils", "Error starting shortcut", e)
    }
}
