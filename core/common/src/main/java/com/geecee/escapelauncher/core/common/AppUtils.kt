package com.geecee.escapelauncher.core.common

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Process
import androidx.core.net.toUri
import com.geecee.escapelauncher.core.model.InstalledApp

import java.text.Normalizer

/**
 * Checks if the app belongs to the main user.
 */
fun InstalledApp.isMainUserApp(): Boolean = this.user == Process.myUserHandle()

/**
 * Opens an app regardless of its profile (Main, Work, or Private).
 */
fun openApp(context: Context, app: InstalledApp, sourceBounds: Rect? = null) {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val options = ActivityOptions.makeBasic()
    if (sourceBounds != null) {
        options.launchBounds = sourceBounds
    }
    
    try {
        launcherApps.startMainActivity(
            app.componentName,
            app.user,
            sourceBounds,
            options.toBundle()
        )
    } catch (e: Exception) {
        // Fallback or logging if needed
    }
}

fun fuzzyMatch(text: String, pattern: String): Boolean {
    // Case-insensitive contains check (original behavior)
    if (text.contains(pattern, ignoreCase = true)) {
        return true
    }

    val regexUnaccent = "\\p{M}+"
    val normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex(regexUnaccent), "")
        .lowercase()

    val normalizedPattern = Normalizer.normalize(pattern, Normalizer.Form.NFD)
        .replace(Regex(regexUnaccent), "")
        .lowercase()

    // Check for initials match (e.g., "gm" matches "Google Maps")
    if (pattern.length >= 2) {
        val words = normalizedText.split(" ")
        if (words.size > 1) {
            val initials = words.joinToString("") { it.firstOrNull()?.toString() ?: "" }
            if (initials.contains(normalizedPattern)) {
                return true
            }
        }
    }

    // Check for character sequence match with gaps
    var textIndex = 0
    var patternIndex = 0
    while (textIndex < normalizedText.length && patternIndex < normalizedPattern.length) {
        if (normalizedText[textIndex] == normalizedPattern[patternIndex]) {
            patternIndex++
        }
        textIndex++
    }

    // If we matched all characters in pattern, it's a fuzzy match
    return patternIndex == normalizedPattern.length
}

/**
 * Sorts a list of apps by relevance to a search query.
 */
fun sortAppsByRelevance(apps: List<InstalledApp>, query: String): List<InstalledApp> {
    val regexUnaccent = "\\p{M}+"
    val normalizedQuery = Normalizer.normalize(query, Normalizer.Form.NFD)
        .replace(Regex(regexUnaccent), "")
        .lowercase()

    return apps.sortedWith(compareBy<InstalledApp> { app ->
        val normalizedName = Normalizer.normalize(app.displayName, Normalizer.Form.NFD)
            .replace(Regex(regexUnaccent), "")
            .lowercase()

        when {
            normalizedName.startsWith(normalizedQuery) -> 0
            normalizedName.contains(normalizedQuery) -> 1
            else -> 2
        }
    }.thenBy { it.displayName.lowercase() })
}

fun uninstallApp(context: Context, app: InstalledApp) {
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:${app.packageName}".toUri()
        putExtra(Intent.EXTRA_USER, app.user)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun goToAppInfo(
    context: Context,
    app: InstalledApp,
    sourceBounds: Rect? = null
) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val profile = app.user

    val options = ActivityOptions.makeBasic()
    if (sourceBounds != null) {
        options.launchBounds = sourceBounds
    }
    launcherApps.startAppDetailsActivity(
        app.componentName,
        profile,
        sourceBounds,
        options.toBundle()
    )
}