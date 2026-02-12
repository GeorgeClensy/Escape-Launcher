package com.geecee.escapelauncher.utils

import android.app.Activity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.geecee.escapelauncher.utils.managers.getSpacerSize

val Activity.spacerSize: Float
    get() = getSpacerSize(this)

fun Activity.setStatusBarImmersive(isSticky: Boolean) {
    val controller = WindowCompat.getInsetsController(window, window.decorView)

    if (isSticky) {
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.statusBars())
    } else {
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
        controller.show(WindowInsetsCompat.Type.statusBars())
    }
}
