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
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import androidx.compose.runtime.MutableState
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.core.graphics.createBitmap
import androidx.core.splashscreen.SplashScreen
import com.geecee.escapelauncher.R
import com.geecee.escapelauncher.ui.views.HomeScreenModel
import com.geecee.escapelauncher.utils.managers.ScreenTimeManager
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
object AppUtils{
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
     * Returns a list of all installed apps on the device
     *
     * @param context Context
     *
     * @return InstalledApp list with all installed apps
     */
    fun getAllInstalledApps(context: Context): List<InstalledApp> {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return emptyList()

        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        val launchableActivities = packageManager.queryIntentActivities(mainIntent, 0).associate {
            it.activityInfo.packageName to ComponentName(
                it.activityInfo.packageName,
                it.activityInfo.name
            )
        }

        return launcherApps.getActivityList(null, myUserHandle())
            .filter { launchableActivities.containsKey(it.applicationInfo.packageName) }
            .map {
                val packageName = it.applicationInfo.packageName
                InstalledApp(
                    displayName = it.label?.toString() ?: "Unknown App",
                    packageName = packageName,
                    componentName = launchableActivities[packageName] ?: it.componentName
                )
            }
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
    fun getCurrentTime(): String {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm") // Format as hours:minutes:seconds
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
                homeScreenModel.pagerState.scrollToPage(1)
                homeScreenModel.appsListScrollState.scrollToItem(0)
            }
            homeScreenModel.searchExpanded.value = false
            homeScreenModel.searchText.value = ""
            homeScreenModel.showBottomSheet.value = false
//            homeScreenModel.loadApps()
            homeScreenModel.reloadFavouriteApps()
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
    fun doHapticFeedBack(context: Context, hapticFeedback: HapticFeedback) {
        if (getBooleanSetting(
                context,
                context.resources.getString(R.string.Haptic),
                true
            )
        ) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /**
     * Splash screen zoom animation
     *
     * @param splashScreen The splash screen to run the animation on
     */
    fun animateSplashScreen(splashScreen: SplashScreen) {
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            // Create a scale animation (zoom effect)
            val scaleAnimation = ScaleAnimation(
                1f, 100f, // From normal size to 5x size
                1f, 100f,
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot at the center
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 1200 // Duration of the zoom animation in ms
                fillAfter = true // Retain the final state
            }

            // Create a fade-out animation
            val fadeOutAnimation = AlphaAnimation(1f, 0f).apply {
                duration = 300 // Duration of the fade-out in ms
                startOffset = 500 // Delay to start after zoom finishes
                fillAfter = true // Retain the final state
            }

            // Combine the animations
            val animationSet = AnimationSet(true).apply {
                addAnimation(scaleAnimation)
                addAnimation(fadeOutAnimation)
            }

            // Start the combined animation
            splashScreenViewProvider.view.startAnimation(animationSet)

            // Remove the splash screen view after the animation ends
            animationSet.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    splashScreenViewProvider.remove()
                }

                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }
    }

    /**
     * Disable or enable analytics,
     *
     * @param enabled Pass as true to enable analytics
     */
    fun configureAnalytics(enabled: Boolean) {
        val analytics = Firebase.analytics

        analytics.setConsent(
            mapOf(
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to if (enabled) FirebaseAnalytics.ConsentStatus.GRANTED else FirebaseAnalytics.ConsentStatus.DENIED,
            )
        )

        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    /**
     * Set a solid color as the home screen wallpaper.
     *
     * @param context The context of the application or activity.
     * @param color The color to set as the wallpaper.
     */
    @Suppress("unused")
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
}
