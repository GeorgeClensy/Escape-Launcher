package com.geecee.escapelauncher.core.model

/**
 * Data class representing an Android app shortcut.
 *
 * App shortcuts provide a way to launch specific actions inside an app directly from the launcher.
 *
 * @property id The unique identifier of the shortcut.
 * @property label The user-visible name of the shortcut.
 * @property rank The display order of the shortcut among others from the same app.
 *               Lower values indicate higher priority.
 */
data class AppShortcut(
    val id: String,
    val label: String,
    val rank: Int
)
