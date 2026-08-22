package com.geecee.escapelauncher.core.common

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Extension function to check if a specific permission is granted
 *
 * @param permission The permission string to check
 * @return True if granted, false otherwise
 */
fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}