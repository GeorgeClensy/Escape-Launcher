package com.geecee.escapelauncher.utils

import android.app.ActivityOptions
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Rect
import android.os.Process.myUserHandle
import android.util.Log
import android.view.Window
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.core.model.InstalledApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.text.Normalizer
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Broadcast receiver to detect when the screen turns off,
 * This is used in Escape Launcher to stop screen time counting if the screen turns off
 */
class ScreenOffReceiver(private val onScreenOff: () -> Unit) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            // When the screen is off, stop screen time tracking
            onScreenOff()
        }
    }
}

/**
 * Set of functions used throughout Escape Launcher app
 *
 * @author George Clensy
 */
object AppUtils {
    /**
     * Function to launch an app.
     *
     * @param context Context
     * @param app The app info being opened
     * @param onAppOpened Callback called when the app is successfully opened for screen time tracking
     *
     * @return Boolean true if the app was launched successfully
     */
    fun launchApp(
        context: Context,
        app: InstalledApp,
        onAppOpened: ((String) -> Unit)? = null
    ): Boolean {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val options = ActivityOptions.makeBasic()

        return try {
            launcherApps.startMainActivity(
                app.componentName,
                myUserHandle(),
                Rect(),
                options.toBundle()
            )
            onAppOpened?.invoke(app.packageName)
            true
        } catch (e: SecurityException) {
            Log.e("AppUtils", "SecurityException opening app: ${e.message}")
            try {
                val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (intent != null) {
                    context.startActivity(intent)
                    onAppOpened?.invoke(app.packageName)
                    true
                } else {
                    false
                }
            } catch (fallbackException: Exception) {
                Log.e("AppUtils", "Failed to launch app even with fallback", fallbackException)
                analyticsProxy.logCustomKey("app_launch_error", app.packageName)
                analyticsProxy.recordException(fallbackException)
                false
            }
        } catch (e: Exception) {
            Log.e("AppUtils", "Error opening app", e)
            false
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
     * 1. Starts with query
     * 2. Contains query
     * 3. Fuzzy match
     * Then alphabetical.
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

    /**
     *  Cache to store package name to app name mappings
     */
    private val appNameCache = mutableMapOf<String, String>()

    /**
     * Returns the app name from its package
     *
     * @param context Context is required
     * @param packageName Name of the package that's app name will be returned
     *
     * @return String app name
     */
    fun getAppNameFromPackageName(context: Context, packageName: String): String {
        // Check cache first for instant return
        appNameCache[packageName]?.let { return it }

        // If not in cache, perform the operation directly but still cache the result
        try {
            val packageManager: PackageManager = context.packageManager
            val applicationInfo: ApplicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(applicationInfo).toString()

            // Cache the result for future use
            appNameCache[packageName] = appName

            return appName
        } catch (_: PackageManager.NameNotFoundException) {
            return "null"
        }
    }

    /**
     * Loads text from a file in Assets
     *
     * @param context Context
     * @param fileName Name of the file text will be loaded from
     *
     * @return Returns a String? with the text from the file
     */
    fun loadTextFromAssets(context: Context, fileName: String): String? {
        var inputStream: InputStream? = null
        var fileContent: String? = null
        try {
            inputStream = context.assets.open(fileName)
            fileContent = inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
        return fileContent
    }


    /**
     * Reset home screen for when app is closed
     */
    fun resetHome(homeScreenModel: HomeScreenModel, shouldGoToFirstPage: Boolean? = true) {
        homeScreenModel.coroutineScope.launch {
            delay(200)
            if (shouldGoToFirstPage == true) {
                homeScreenModel.mainAppViewModel.requestToGoHome()
            }
            homeScreenModel.showBottomSheet.value = false
//            homeScreenModel.loadApps()
            homeScreenModel.showWorkApps.value = false
            homeScreenModel.showWorkBottomSheet.value = false
        }
    }

    /**
     * Disable or enable analytics,
     *
     * @param enabled Pass as true to enable analytics
     */
    fun configureAnalytics(context: Context, enabled: Boolean) {
        analyticsProxy.configureAnalytics(context, enabled)
        Log.d("Analytics", "Anayitcs are $enabled")
    }

    /**
     * Set a solid color as the home screen wallpaper.
     *
     * @param context The context of the application or activity.
     * @param color The color to set as the wallpaper.
     */
    fun setSolidColorWallpaperHomeScreen(context: Context, color: ComposeColor) {
        val wallpaperManager = WallpaperManager.getInstance(context)

        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val bitmap = createBitmap(width, height)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = Paint().apply {
            this.color = color.toAndroidColor()
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
    }

    /**
     * Convert a Compose Color to an Android Color.
     *
     * @return The Android Color as an integer.
     */
    fun ComposeColor.toAndroidColor(): Int {
        return AndroidColor.argb(
            (alpha * 255).toInt(),
            (red * 255).toInt(),
            (green * 255).toInt(),
            (blue * 255).toInt()
        )
    }

    fun getInstalledAppFromPackageName(context: Context, packageName: String): InstalledApp? {
        return try {
            val pm: PackageManager = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val displayName = pm.getApplicationLabel(appInfo).toString()
            val launchIntent = pm.getLaunchIntentForPackage(packageName)

            // Some apps might not have a launchable activity
            val componentName = launchIntent?.component ?: ComponentName(packageName, "")

            InstalledApp(
                displayName = displayName,
                packageName = packageName,
                componentName = componentName
            )
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            null
        }
    }


    fun configureOnboardingFullScreen(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}
