package com.geecee.escapelauncher.utils

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
import com.geecee.escapelauncher.R

/*
 * Check if the device has secure folder installed just in the main user profile
 * Secure folder was introduced in Android Pie
 */
@RequiresApi(Build.VERSION_CODES.P)
fun canUseSecureFolder(context: Context): Boolean {
    return try {
        context.packageManager.getPackageInfo(
            "com.samsung.knox.securefolder",
            0
        )
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}

/*
 * Check if the Secure folder user profile is already created
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

/*
 * Launch the secure folder UI as an alternative.
 * Samsung restricts listing apps from the secure folder.
 */
fun launchSecureFolder(context: Context) {

    val secureFolderProfile = getSecureFolderProfile(context)
    val launcherApps = context.getSystemService(LauncherApps::class.java)

    if (secureFolderProfile != null && launcherApps != null) {
        // Find the actual launcher activity from the profile
        val activityInfo = launcherApps.getActivityList(null, secureFolderProfile)
            .firstOrNull { it.componentName.packageName == "com.samsung.knox.securefolder" }

        if (activityInfo != null) {
            try {
                launcherApps.startMainActivity(
                    activityInfo.componentName,
                    secureFolderProfile,
                    null,
                    null
                )
                return
            } catch (_: Exception) {
            }
        }
    }

    // If the user has disabled Secure Folder, redirect to Settings and show a toast
    Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }.also { context.startActivity(it) }

    Toast.makeText(context, R.string.enable_secure_folder_on_settings, Toast.LENGTH_LONG).show()
}
