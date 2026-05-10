package com.geecee.escapelauncher.core.ui.model

import androidx.annotation.StringRes
import com.geecee.escapelauncher.core.model.InstalledApp

/**
 * Action that can be shown in the bottom sheet or other menus.
 *
 * @param label Literal string label (used for system shortcuts).
 * @param labelRes String resource ID label (used for standard actions).
 * @param isVisible A lambda to determine if this action should be shown for a specific app.
 * @param onClick The callback to execute when the action is clicked.
 */
data class AppAction(
    val label: String? = null,
    @StringRes val labelRes: Int? = null,
    val isVisible: (InstalledApp) -> Boolean = { true },
    val onClick: (InstalledApp) -> Unit
)
