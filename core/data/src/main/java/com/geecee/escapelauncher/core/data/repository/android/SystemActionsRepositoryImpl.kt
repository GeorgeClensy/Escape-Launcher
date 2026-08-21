package com.geecee.escapelauncher.core.data.repository.android

import android.Manifest
import android.app.ActivityOptions
import android.app.WallpaperManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri
import com.geecee.escapelauncher.core.common.EscapeAccessibilityService
import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemActionsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SystemActionsRepository {

    override fun uninstallApp(app: InstalledApp) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = "package:${app.packageName}".toUri()
            putExtra(Intent.EXTRA_USER, app.user)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun openAppDetails(app: InstalledApp, sourceBounds: Rect?) {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps ?: return
        val options = ActivityOptions.makeBasic()
        if (sourceBounds != null) {
            options.launchBounds = sourceBounds
        }
        launcherApps.startAppDetailsActivity(
            app.componentName,
            app.user,
            sourceBounds,
            options.toBundle()
        )
    }

    override fun isDefaultLauncher(): Boolean {
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

    override fun promptSetDefaultLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager?.isRoleAvailable(RoleManager.ROLE_HOME) == true) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                openLauncherSettings()
            }
        } else {
            openLauncherSettings()
        }
    }

    private fun openLauncherSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    @RequiresPermission(Manifest.permission.SET_WALLPAPER)
    override fun setSolidColorWallpaper(color: Int) {
        val wallpaperManager = WallpaperManager.getInstance(context)

        val displayMetrics = context.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        try {
            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun lockScreen() {
        EscapeAccessibilityService.instance?.lockScreen()
    }
}
