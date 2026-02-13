package com.geecee.escapelauncher.core.model

import android.content.ComponentName

/**
 * Data class representing an app
 */
data class InstalledApp(
    var displayName: String,
    var packageName: String,
    var componentName: ComponentName
)
