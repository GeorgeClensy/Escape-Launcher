package com.geecee.escapelauncher.core.common

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri

fun uninstallApp(context: Context, app: InstalledApp){
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = "package:${app.packageName}".toUri()
        putExtra(Intent.EXTRA_USER, app.user)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

fun goToAppInfo(context: Context, app: InstalledApp){
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = "package:${app.packageName}".toUri()
        putExtra(Intent.EXTRA_USER, app.user)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}