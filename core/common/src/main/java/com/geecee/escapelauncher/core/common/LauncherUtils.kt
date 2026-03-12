package com.geecee.escapelauncher.core.common

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

/**
 * Finds out if Escape Launcher is the default launcher
 *
 * @return Boolean which will be true if it is the default launcher
 */
fun isDefaultLauncher(context: Context): Boolean {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
    }
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfo?.activityInfo?.packageName == context.packageName
}