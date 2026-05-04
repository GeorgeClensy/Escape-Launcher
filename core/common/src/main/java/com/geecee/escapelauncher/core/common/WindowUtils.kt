package com.geecee.escapelauncher.core.common

import android.os.Build
import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Puts the app into full screen
 *
 * @param window The window to make full screen
 *
 * @author George Clensy
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