package com.geecee.escapelauncher.core.common

import android.content.ComponentName
import android.os.Process.myUserHandle
import android.os.UserHandle

/**
 * Data class representing an app
 */
data class InstalledApp(
    var displayName: String,
    var packageName: String,
    var componentName: ComponentName,
    val user: UserHandle = myUserHandle()
)