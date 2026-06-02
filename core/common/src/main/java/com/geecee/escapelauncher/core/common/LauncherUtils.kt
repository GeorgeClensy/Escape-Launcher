package com.geecee.escapelauncher.core.common

import android.Manifest
import android.app.Activity
import android.app.WallpaperManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.provider.Settings
import androidx.annotation.ColorInt
import androidx.annotation.RequiresPermission
import androidx.core.graphics.createBitmap

private const val REQUEST_ROLE_HOME_CODE = 678

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
 * Finds out if Escape Launcher is the default launcher
 *
 * @return Boolean which will be true if it is the default launcher
 */
fun isDefaultLauncher(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = context.getSystemService(RoleManager::class.java)
        roleManager?.isRoleHeld(RoleManager.ROLE_HOME) == true
    } else {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        resolveInfo?.activityInfo?.packageName == context.packageName
    }
}

/**
 * Set a solid colour as the home screen wallpaper.
 *
 * @param context The context of the application or activity.
 * @param color The colour to set as the wallpaper.
 */
@RequiresPermission(Manifest.permission.SET_WALLPAPER)
fun setSolidColorWallpaperHomeScreen(context: Context, @ColorInt colour: Int) {
    val wallpaperManager = WallpaperManager.getInstance(context)

    val displayMetrics = context.resources.displayMetrics
    val width = displayMetrics.widthPixels
    val height = displayMetrics.heightPixels

    val bitmap = createBitmap(width, height)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        this.color = colour
        style = Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
}