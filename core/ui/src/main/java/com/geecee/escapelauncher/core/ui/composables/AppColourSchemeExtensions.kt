package com.geecee.escapelauncher.core.ui.composables

import androidx.annotation.StringRes
import com.geecee.escapelauncher.core.theme.AppColourScheme
import com.geecee.escapelauncher.core.ui.R

@StringRes
fun AppColourScheme.nameRes(): Int = when (this) {
    AppColourScheme.MONOCHROME -> R.string.monochrome
    AppColourScheme.RED -> R.string.red
    AppColourScheme.GREEN -> R.string.green
    AppColourScheme.BLUE -> R.string.blue
    AppColourScheme.YELLOW -> R.string.yellow
    AppColourScheme.OFF_LIGHT -> R.string.off_white
    AppColourScheme.SYSTEM -> R.string.system
    AppColourScheme.ESCAPE_THEME -> R.string.escape_launcher
}
