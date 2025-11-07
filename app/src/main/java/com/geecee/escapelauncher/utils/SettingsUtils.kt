package com.geecee.escapelauncher.utils

import android.app.Activity
import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.core.content.edit
import com.geecee.escapelauncher.MainHomeScreen
import com.geecee.escapelauncher.R

/**
 * Shows the launcher selector
 */
fun Activity.showLauncherSelector() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
        if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            startActivityForResult(intent, REQUEST_ROLE_HOME_CODE)
        } else {
            showLauncherSettingsMenu(this)
        }
    } else {
        showLauncherSettingsMenu(this)
    }
}

/**
 * Shows the select launcher menu in system settings
 */
fun showLauncherSettingsMenu(activity: Context) {
    val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    activity.startActivity(intent)
}

/**
 * Returns the package of the current default launcher
 */
fun getDefaultLauncherPackage(context: Context): String {
    val intent = Intent()
    intent.action = Intent.ACTION_MAIN
    intent.addCategory(Intent.CATEGORY_HOME)
    val packageManager = context.packageManager
    val result = packageManager.resolveActivity(intent, 0)
    return if (result?.activityInfo != null) {
        result.activityInfo.packageName
    } else "android"
}

/**
 * Returns if the default launcher is escape launcher
 */
fun isDefaultLauncher(context: Context): Boolean {
    val launcherPackageName = getDefaultLauncherPackage(context)
    return "com.geecee.escapelauncher" == launcherPackageName || "com.geecee.escapelauncher.dev" == launcherPackageName
}

private const val REQUEST_ROLE_HOME_CODE = 678

/**
 * Change home alignment
 */
fun changeHomeAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putString(
            context.resources.getString(R.string.HomeAlignment), when (alignment) {
                1 -> "Center"
                0 -> "Left"
                else -> "Right"
            }
        )

    }
}

/**
 * Get the home alignment as an integer
 */
fun getHomeAlignmentAsInt(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeAlignment),
            "Center"
        ) == "Left"
    ) {
        0
    } else if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeAlignment),
            "Center"
        ) == "Center"
    ) {
        1
    } else {
        2
    }
}

/**
 * Get the home alignment as an Alignment.Horizontal
 */
fun getHomeAlignment(context: Context): Alignment.Horizontal {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeAlignment),
            "Center"
        ) == "Left"
    ) {
        Alignment.Start
    } else if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeAlignment),
            "Center"
        ) == "Center"
    ) {
        Alignment.CenterHorizontally
    } else {
        Alignment.End
    }
}

/**
 * Change the vertical alignment of the home screen
 */
fun changeHomeVAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putString(
            context.resources.getString(R.string.HomeVAlignment), when (alignment) {
                1 -> "Center"
                0 -> "Top"
                else -> "Bottom"
            }
        )

    }
}

/**
 * Get the home vertical alignment as an integer
 */
fun getHomeVAlignmentAsInt(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeVAlignment),
            "Center"
        ) == "Top"
    ) {
        0
    } else if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeVAlignment),
            "Center"
        ) == "Center"
    ) {
        1
    } else {
        2
    }
}

/**
 * Get the home vertical alignment
 */
fun getHomeVAlignment(context: Context): Arrangement.Vertical {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeVAlignment),
            "Center"
        ) == "Top"
    ) {
        Arrangement.Top
    } else if (sharedPreferences.getString(
            context.resources.getString(R.string.HomeVAlignment),
            "Center"
        ) == "Center"
    ) {
        Arrangement.Center
    } else {
        Arrangement.Bottom
    }
}

/**
 * Change the apps alignment
 */
fun changeAppsAlignment(context: Context, alignment: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        putString(
            context.resources.getString(R.string.AppsAlignment), when (alignment) {
                1 -> "Center"
                0 -> "Left"
                else -> "Right"
            }
        )

    }
}

/**
 * Get the alignment
 */
fun getAppsAlignmentAsInt(context: Context): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(
            context.resources.getString(R.string.AppsAlignment),
            "Center"
        ) == "Left"
    ) {
        0
    } else if (sharedPreferences.getString(
            context.resources.getString(R.string.AppsAlignment),
            "Center"
        ) == "Center"
    ) {
        1
    } else {
        2
    }
}

/**
 * Get the alignment of the apps
 */
fun getAppsAlignment(context: Context): Alignment.Horizontal {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return if (sharedPreferences.getString(
            context.resources.getString(R.string.AppsAlignment),
            "Center"
        ) == "Left"
    ) {
        Alignment.Start
    } else if (sharedPreferences.getString(
            context.resources.getString(R.string.AppsAlignment),
            "Center"
        ) == "Center"
    ) {
        Alignment.CenterHorizontally
    } else {
        Alignment.End
    }
}

// Generic functions

fun toggleBooleanSetting(context: Context, shouldTurnOn: Boolean, setting: String) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {

        if (shouldTurnOn) {
            putBoolean(setting, true)
        } else {
            putBoolean(setting, false)
        }

    }
}

fun getBooleanSetting(context: Context, setting: String): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )

    return sharedPreferences.getBoolean(setting, false)
}

fun getStringSetting(context: Context, setting: String, defaultValue: String): String {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    return sharedPreferences.getString(setting, defaultValue) ?: defaultValue
}

fun setStringSetting(context: Context, setting: String, value: String) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {
        putString(setting, value)
    }
}

fun getIntSetting(context: Context, setting: String, defaultValue: Int): Int {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    return sharedPreferences.getInt(setting, defaultValue)
}

fun setIntSetting(context: Context, setting: String, value: Int) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {
        putInt(setting, value)
    }
}

fun getBooleanSetting(context: Context, setting: String, defaultValue: Boolean): Boolean {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    return sharedPreferences.getBoolean(setting, defaultValue)
}

fun setBooleanSetting(context: Context, setting: String, value: Boolean) {
    val sharedPreferences = context.getSharedPreferences(
        R.string.settings_pref_file_name.toString(), Context.MODE_PRIVATE
    )
    sharedPreferences.edit {
        putBoolean(setting, value)
    }
}

fun resetActivity(context: Context, activity: Activity) {
    val intent = Intent(context, MainHomeScreen::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val options = ActivityOptions.makeBasic()
    context.startActivity(intent, options.toBundle())
    activity.finish()
}