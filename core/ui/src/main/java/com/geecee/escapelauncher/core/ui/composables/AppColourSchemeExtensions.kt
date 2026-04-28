package com.geecee.escapelauncher.core.ui.composables

import androidx.annotation.StringRes
import com.geecee.escapelauncher.core.theme.AppColourScheme
import com.geecee.escapelauncher.core.ui.R

@StringRes
fun AppColourScheme.nameRes(): Int = when (this) {
    AppColourScheme.DARK -> R.string.dark
    AppColourScheme.LIGHT -> R.string.light
    AppColourScheme.PITCH_DARK -> R.string.pitch_black

    AppColourScheme.LIGHT_RED -> R.string.light_red
    AppColourScheme.DARK_RED -> R.string.dark_red

    AppColourScheme.LIGHT_GREEN -> R.string.light_green
    AppColourScheme.DARK_GREEN -> R.string.dark_green

    AppColourScheme.LIGHT_BLUE -> R.string.light_blue
    AppColourScheme.DARK_BLUE -> R.string.dark_blue

    AppColourScheme.LIGHT_YELLOW -> R.string.light_yellow
    AppColourScheme.DARK_YELLOW -> R.string.dark_yellow

    AppColourScheme.OFF_LIGHT -> R.string.off_white
    AppColourScheme.SYSTEM -> R.string.system
}

@StringRes
fun AppColourScheme.Companion.nameResFromId(id: Int): Int =
    AppColourScheme.fromId(id).nameRes()