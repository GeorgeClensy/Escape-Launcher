package com.geecee.escapelauncher.feature.securefolder

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.geecee.escapelauncher.core.ui.R

//todo: this should probably go in a domain or something
/**
 * Checks if the device has `com.samsung.knox.securefolder` installed with `context.packageManager.getPackageInfo()`
 * Requires Android Pie (API 29) or higher as secure folder was added in Android Pie.
 *
 * @param context The application context
 * @return true if the device has secure folder installed
 * @author George Clensy
 */
@RequiresApi(Build.VERSION_CODES.P)
fun canUseSecureFolder(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo(
            "com.samsung.knox.securefolder", 0
        )
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * Checks if the device has a `UserHandle` with `com.samsung.knox.securefolder` installed.
 * @return Will return the `UserHandle` for `com.samsung.knox.securefolder` if it exists and will return `null` if it does not
 * @param context The application context
 * @author George Clensy
 */
fun getSecureFolderProfile(context: Context): UserHandle? {
    val launcherApps = context.getSystemService(LauncherApps::class.java)
    val userManager = context.getSystemService(UserManager::class.java) ?: return null
    val profiles = userManager.userProfiles

    return profiles.find { profile ->
        launcherApps.getActivityList(null, profile)
            .any { it.componentName.packageName == "com.samsung.knox.securefolder" }
    }
}

/**
 * Launches the Samsung Secure Folder UI as samsung restrics listing and opening apps from the secure folder from other launchers.
 *
 * @param context The application context
 * @author George Clensy
 */
fun launchSecureFolder(context: Context) {
    // Gets the secure folder profile
    val secureFolderProfile = getSecureFolderProfile(context)
    val launcherApps = context.getSystemService(LauncherApps::class.java)

    // If the user has enabled Secure Folder, launch the UI
    if (secureFolderProfile != null && launcherApps != null) {
        // Find the actual launcher activity from within the profile
        val activityInfo = launcherApps.getActivityList(null, secureFolderProfile)
            .firstOrNull { it.componentName.packageName == "com.samsung.knox.securefolder" }

        if (activityInfo != null) {
            try {
                launcherApps.startMainActivity(
                    activityInfo.componentName, secureFolderProfile, null, null
                )
                return
            } catch (_: Exception) {
                //todo: do something here
            }
        }
    }

    // If the user has disabled Secure Folder, redirect to Settings and show a toast
    Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.also { context.startActivity(it) }

    Toast.makeText(context, R.string.enable_secure_folder_on_settings, Toast.LENGTH_LONG).show()
}
