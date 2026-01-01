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
import android.os.Build
import android.os.Process.myUserHandle
import android.util.Log
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.core.graphics.createBitmap
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.theme.AppTheme
import com.geecee.escapelauncher.ui.theme.EscapeTheme
import com.geecee.escapelauncher.utils.managers.ScreenTimeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
import com.geecee.escapelauncher.MainAppViewModel as MainAppModel

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
 * Data class representing an app
 */
data class InstalledApp(
    var displayName: String,
    var packageName: String,
    var componentName: ComponentName
)

/**
 * Set of functions used throughout Escape Launcher app
 *
 * @author George Clensy
 */
object AppUtils {
    /**
     * Function to open app.
     * [openChallengeShow] will be set to true if the app has a challenge in the challenge manager. This is so you can use the OpenChallenge function with this, if you do not want to use open challenges set this to null and [overrideOpenChallenge] to true
     *
     * @param app The app info being opened
     * @param overrideOpenChallenge Whether the open challenge should be skipped
     * @param openChallengeShow This is set to true if the app has an open challenge, We recommend having a composable that shows when that's true to act as the open challenge
     * @param mainAppModel Main view model, needed for open challenge manager, package manager, context
     *
     * @author George Clensy
     */
    fun openApp(
        app: InstalledApp,
        mainAppModel: MainAppModel,
        homeScreenModel: HomeScreenModel,
        overrideOpenChallenge: Boolean,
        openChallengeShow: MutableState<Boolean>?
    ) {
        val launcherApps = mainAppModel.getContext()
            .getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val options = ActivityOptions.makeBasic()

        if (!mainAppModel.challengesManager.doesAppHaveChallenge(app.packageName) || overrideOpenChallenge) {
            launcherApps.startMainActivity(
                app.componentName,
                myUserHandle(),
                Rect(),
                options.toBundle()
            )
            ScreenTimeManager.onAppOpened(app.packageName)

            mainAppModel.isAppOpened = true
            mainAppModel.shouldGoHomeOnResume.value = true
            homeScreenModel.updateSelectedApp(app)
        } else {
            if (openChallengeShow != null) {
                openChallengeShow.value = true
            }
        }
    }

    fun fuzzyMatch(text: String, pattern: String): Boolean {
        // Case-insensitive contains check (original behavior)
        if (text.contains(pattern, ignoreCase = true)) {
            return true
        }

        val lowerText = text.lowercase()
        val lowerPattern = pattern.lowercase()

        // Check for initials match (e.g., "gm" matches "Google Maps")
        if (pattern.length >= 2) {
            val words = lowerText.split(" ")
            if (words.size > 1) {
                val initials = words.joinToString("") { it.firstOrNull()?.toString() ?: "" }
                if (initials.contains(lowerPattern)) {
                    return true
                }
            }
        }

        // Check for character sequence match with gaps
        var textIndex = 0
        var patternIndex = 0
        while (textIndex < lowerText.length && patternIndex < lowerPattern.length) {
            if (lowerText[textIndex] == lowerPattern[patternIndex]) {
                patternIndex++
            }
            textIndex++
        }

        // If we matched all characters in pattern, it's a fuzzy match
        return patternIndex == lowerPattern.length
    }

    /**
     * Sorts a list of apps by relevance to a search query.
     * 1. Starts with query
     * 2. Contains query
     * 3. Fuzzy match
     * Then alphabetical.
     */
    fun sortAppsByRelevance(apps: List<InstalledApp>, query: String): List<InstalledApp> {
        val queryLower = query.lowercase()
        return apps.sortedWith(compareBy<InstalledApp> { app ->
            val nameLower = app.displayName.lowercase()
            when {
                nameLower.startsWith(queryLower) -> 0
                nameLower.contains(queryLower) -> 1
                else -> 2
            }
        }.thenBy { it.displayName.lowercase() })
    }

    /**
     * Returns a list of all installed apps on the device that have a launcher activity.
     *
     * @param context Context
     *
     * @return InstalledApp list with all installed apps that can be launched.
     */
    fun getAllInstalledApps(context: Context): List<InstalledApp> {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Get all activities that can be launched from a launcher
        val launchableActivities = packageManager.queryIntentActivities(mainIntent, 0)

        return launchableActivities
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)

                if (launchIntent != null && launchIntent.component != null) {
                    InstalledApp(
                        displayName = resolveInfo.loadLabel(packageManager).toString(),
                        packageName = packageName,
                        componentName = launchIntent.component!! // Use the component from the launch intent
                    )
                } else {
                    null // Filter out apps that don't have a valid launch intent or component
                }
            }
            .distinctBy { it.packageName } // Ensure only one entry per package
    }

    /**
     * Formats screen time into string in the style of 5h 3m
     *
     * @param milliseconds The amount of time to return formatted
     *
     * @author George Clensy
     *
     * @return Returns a string that looks like this: 5h 3m
     */
    fun formatScreenTime(milliseconds: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${seconds}s"
        }
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
     * Returns the current time as a string
     *
     * @return String the time with the format HH:mm
     */
    fun getCurrentTime(twelveHour: Boolean): String {
        val now = LocalTime.now()
        var formatter = DateTimeFormatter.ofPattern("HH:mm") // Format as hours:minutes:seconds
        if (twelveHour) {
            formatter = DateTimeFormatter.ofPattern("hh:mm")
        }
        return now.format(formatter)
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
     * Finds out if Escape Launcher is the default launcher
     *
     * @return Boolean which will be true if it is the default launcher
     */
    fun isDefaultLauncher(context: Context): Boolean {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    /**
     * Reset home screen for when app is closed
     */
    fun resetHome(homeScreenModel: HomeScreenModel, shouldGoToFirstPage: Boolean? = true) {
        homeScreenModel.coroutineScope.launch {
            delay(200)
            if (shouldGoToFirstPage == true) {
                homeScreenModel.goToMainPage()
                homeScreenModel.appsListScrollState.scrollToItem(0)
            }
            homeScreenModel.searchExpanded.value = false
            homeScreenModel.searchText.value = ""
            homeScreenModel.showBottomSheet.value = false
//            homeScreenModel.loadApps()
            homeScreenModel.reloadFavouriteApps()
            homeScreenModel.showWorkApps.value = false
            homeScreenModel.showWorkBottomSheet.value = false
        }
    }

    /**
     * Returns the date yesterday as a string
     *
     * @return String formatted yyyy-MM-dd
     */
    fun getYesterday(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayDate =
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        return yesterdayDate
    }

    /**
     * Performs haptic feedback
     */
    fun doHapticFeedBack(hapticFeedback: HapticFeedback) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /**
     * Disable or enable analytics,
     *
     * @param enabled Pass as true to enable analytics
     */
    fun configureAnalytics(context: Context, enabled: Boolean) {
        analyticsProxy.configureAnalytics(context, enabled)
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

    /**
     * Puts the app into full screen
     */
    @Suppress("DEPRECATION")
    fun configureFullScreenMode(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.navigationBars()) // Show navigation bars
        controller.hide(WindowInsetsCompat.Type.statusBars()) // hide status bar only
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_DEFAULT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false)
        }
    }

    fun configureOnboardingFullScreen(window: Window) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    /**
     * Sets up theme by retrieving theme that should be used and then passing it and the content into an EscapeTheme composable
     */
    @Composable
    fun SetUpTheme(content: @Composable () -> Unit, viewModel: MainAppModel) {
        val context = LocalContext.current
        val config = LocalConfiguration.current
        val resources = LocalResources.current

        androidx.compose.runtime.LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                Log.d("Loading","Theme loading started")

                var settingToChange = resources.getString(R.string.Theme)

                if (getBooleanSetting(
                        context,
                        resources.getString(R.string.autoThemeSwitch),
                        false
                    )
                ) {
                    val isDark = (config.uiMode and
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                            android.content.res.Configuration.UI_MODE_NIGHT_YES

                    settingToChange = if (isDark) {
                        resources.getString(R.string.dTheme)
                    } else {
                        resources.getString(R.string.lTheme)
                    }
                }

                // Get theme ID
                val themeId = getIntSetting(context, settingToChange, 11)

                withContext(Dispatchers.Main) {
                    viewModel.appTheme.value = AppTheme.fromId(themeId)
                    viewModel.isThemeLoaded.value = true
                }
            }
        }

        EscapeTheme(viewModel.appTheme.value) {
            content()
        }
    }
}
