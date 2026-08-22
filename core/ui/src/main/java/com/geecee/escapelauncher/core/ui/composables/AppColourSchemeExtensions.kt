package com.geecee.escapelauncher.core.ui.composables

import androidx.annotation.StringRes
import com.geecee.escapelauncher.core.theme.colours.AppColourScheme
import com.geecee.escapelauncher.core.ui.R

@StringRes
fun AppColourScheme.nameRes(): Int = when (this) {
    AppColourScheme.MONOCHROME -> R.string.monochrome
    AppColourScheme.RED -> R.string.red
    AppColourScheme.GREEN -> R.string.green
    AppColourScheme.BLUE -> R.string.blue
    AppColourScheme.TEAL -> R.string.teal
    AppColourScheme.PURPLE -> R.string.purple
    AppColourScheme.PINK -> R.string.pink
    AppColourScheme.YELLOW -> R.string.yellow
    AppColourScheme.ORANGE -> R.string.orange
    AppColourScheme.SYSTEM -> R.string.system
    AppColourScheme.ESCAPE_THEME -> R.string.escape_launcher
}
