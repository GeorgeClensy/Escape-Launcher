package com.geecee.escapelauncher.core.model

import android.content.ComponentName
import android.os.Process
import android.os.UserHandle

/**
 * Data class representing an app
 */
data class InstalledApp(
    val displayName: String,
    val packageName: String,
    val componentName: ComponentName,
    val user: UserHandle = Process.myUserHandle()
)