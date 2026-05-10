/**
 * @author George Clensy
 * Utility functions and UI components for managing and interacting with Private Space in Escape Launcher.
 */

package com.geecee.escapelauncher.core.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.os.Build
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import com.geecee.escapelauncher.core.model.InstalledApp

const val PRIVATE_SPACE_USER_TYPE = "android.os.usertype.profile.PRIVATE"

/**
 * BroadcastReceiver that listens for Private Space state changes (locked/unlocked).
 */
class PrivateSpaceStateReceiver(private val onStateChange: (Boolean) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PROFILE_AVAILABLE -> onStateChange(true) // Private space is unlocked
            Intent.ACTION_PROFILE_UNAVAILABLE -> onStateChange(false) // Private space is locked
        }
    }
}

/**
 * Determines whether Private Space is currently unlocked.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun isPrivateSpaceUnlocked(context: Context): Boolean {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return false
    val userManager = getSystemService(context, UserManager::class.java) ?: return false

    val privateUser = userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    } ?: return false

    return !userManager.isQuietModeEnabled(privateUser)
}

/**
 * Locks Private Space by enabling Quiet Mode.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun lockPrivateSpace(context: Context) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val userManager = getSystemService(context, UserManager::class.java) ?: return

    userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    }?.let { userManager.requestQuietModeEnabled(true, it) }
}

/**
 * Unlocks Private Space by disabling Quiet Mode.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun unlockPrivateSpace(context: Context) {
    val launcherApps =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
    val userManager = getSystemService(context, UserManager::class.java) ?: return

    userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    }?.let { userManager.requestQuietModeEnabled(false, it) }
}

/**
 * Retrieves a list of installed apps in Private Space.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun getPrivateSpaceApps(context: Context): List<InstalledApp> {
    val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
        ?: return emptyList()
    val userManager = getSystemService(context, UserManager::class.java) ?: return emptyList()
    val privateUser = userManager.userProfiles.find {
        launcherApps.getLauncherUserInfo(it)?.userType == PRIVATE_SPACE_USER_TYPE
    } ?: return emptyList()

    return launcherApps.getActivityList(null, privateUser).map {
        InstalledApp(
            displayName = it.label?.toString() ?: "Unknown App",
            packageName = it.applicationInfo.packageName,
            componentName = it.componentName,
            user = privateUser
        )
    }
}

/**
 * Checks if a Private Space profile exists on the device.
 */
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun doesPrivateSpaceExist(context: Context): Boolean {
    val userManager = getSystemService(context, UserManager::class.java) as UserManager
    val profiles = userManager.userProfiles

    for (userInfo in profiles) {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        if (launcherApps.getLauncherUserInfo(userInfo)?.userType == "android.os.usertype.profile.PRIVATE") {
            return true
        }
    }
    return false
}
