package com.geecee.escapelauncher.core.common

import android.os.Build
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Configure the status bar visibility
 *
 * @param hide Whether to hide the status bar
 */
fun Window.configureStatusBar(hide: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    val controller = WindowCompat.getInsetsController(this, decorView)
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    if (hide) {
        controller.hide(WindowInsetsCompat.Type.statusBars())
    } else {
        controller.show(WindowInsetsCompat.Type.statusBars())
    }
}

/**
 * Configure the navigation bar visibility
 *
 * @param hide Whether to hide the navigation bar
 */
fun Window.configureNavBar(hide: Boolean) {
    WindowCompat.setDecorFitsSystemWindows(this, false)
    val controller = WindowCompat.getInsetsController(this, decorView)
    controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    if (hide) {
        controller.hide(WindowInsetsCompat.Type.navigationBars())
    } else {
        controller.show(WindowInsetsCompat.Type.navigationBars())
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isNavigationBarContrastEnforced = false
    }
}
