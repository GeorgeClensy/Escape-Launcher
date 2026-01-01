/**
 * @author George Clensy
 * Utility functions and UI components for managing and interacting with Work Profile in Escape Launcher.
 */

package com.geecee.escapelauncher.utils

import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.graphics.Rect
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.os.UserManager.USER_TYPE_PROFILE_MANAGED
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.net.toUri
import com.geecee.escapelauncher.HomeScreenModel
import com.geecee.escapelauncher.ui.theme.ContentColor
import com.geecee.escapelauncher.utils.AppUtils.resetHome

/**
 * BroadcastReceiver that listens for Work Profile state changes (locked/unlocked).
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class WorkProfileStateReceiver(private val onStateChange: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PROFILE_AVAILABLE -> onStateChange(true) // Work profile is unlocked
            Intent.ACTION_PROFILE_UNAVAILABLE -> onStateChange(false) // Work profile is locked
        }
    }
}

/**
 * Checks if a Work Profile exists on the device.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun doesWorkProfileExist(context: Context): Boolean {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return false
    val userManager = getSystemService(context, UserManager::class.java) ?: return false

    for (userInfo in userManager.userProfiles) {
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == USER_TYPE_PROFILE_MANAGED) {
            return true
        }
    }

    return false
}

/**
 * Retrieves the UserHandle for the Work Profile.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun getWorkProfile(context: Context): UserHandle? {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return null
    val userManager = getSystemService(context, UserManager::class.java) ?: return null

    return userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == USER_TYPE_PROFILE_MANAGED
    }
}

/**
 * Determines whether Work Profile is currently unlocked.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun isWorkProfileUnlocked(context: Context): Boolean {
    val userManager = getSystemService(context, UserManager::class.java) ?: return false
    val workProfile = getWorkProfile(context) ?: return false
    return !userManager.isQuietModeEnabled(workProfile)
}

/**
 * Locks Work Profile by enabling Quiet Mode.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun lockWorkProfile(context: Context) {
    val userManager = getSystemService(context, UserManager::class.java) ?: return
    getWorkProfile(context)?.let { userHandle ->
        userManager.requestQuietModeEnabled(true, userHandle)
    }
}

/**
 * Unlocks Work Profile by disabling Quiet Mode.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun unlockWorkProfile(context: Context) {
    val userManager = getSystemService(context, UserManager::class.java) ?: return
    getWorkProfile(context)?.let { userHandle ->
        userManager.requestQuietModeEnabled(false, userHandle)
    }
}

/**
 * Retrieves a list of installed apps in Work Profile.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun getWorkApps(context: Context): List<InstalledApp> {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return emptyList()
    val workProfile = getWorkProfile(context) ?: return emptyList()

    return launcherApps.getActivityList(null, workProfile).map {
        InstalledApp(
            displayName = it.label?.toString() ?: "Unknown App",
            packageName = it.applicationInfo.packageName,
            componentName = it.componentName
        )
    }
}

/**
 * Shows the system app info page for an app in Work Profile.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun goToWorkAppAppInfo(
    installedApp: InstalledApp,
    homeScreenModel: HomeScreenModel,
    context: Context,
    sourceBounds: Rect? = null
) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val workProfile = getWorkProfile(context) ?: return

    val options = ActivityOptions.makeBasic()
    if (sourceBounds != null) {
        options.launchBounds = sourceBounds
    }
    launcherApps.startAppDetailsActivity(
        installedApp.componentName,
        workProfile,
        sourceBounds,
        options.toBundle()
    )
    resetHome(homeScreenModel, shouldGoToFirstPage = true)
}

/**
 * Uninstalls an app from Work Profile.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun uninstallWorkApp(installedApp: InstalledApp, context: Context) {
    val workProfile = getWorkProfile(context) ?: return

    val uninstallIntent = Intent(Intent.ACTION_DELETE)
    uninstallIntent.data = "package:${installedApp.packageName}".toUri()
    uninstallIntent.putExtra(Intent.EXTRA_USER, workProfile)
    uninstallIntent.putExtra(Intent.EXTRA_RETURN_RESULT, true)
    uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(uninstallIntent)
}

/**
 * Opens app in Work Profile.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun openWorkApp(installedApp: InstalledApp, context: Context, sourceBounds: Rect? = null) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val workProfile = getWorkProfile(context) ?: return

    val options = ActivityOptions.makeBasic()
    launcherApps.startMainActivity(
        installedApp.componentName,
        workProfile,
        sourceBounds,
        options.toBundle()
    )
}

/**
 * UI component for displaying a single Work Profile app item.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkAppItem(
    appName: String,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val modifier = Modifier
        .padding(vertical = 15.dp)
        .combinedClickable(onClick = onClick, onLongClick = onLongClick)

    Text(
        appName,
        modifier = modifier,
        color = ContentColor,
        style = MaterialTheme.typography.bodyMedium
    )
}
