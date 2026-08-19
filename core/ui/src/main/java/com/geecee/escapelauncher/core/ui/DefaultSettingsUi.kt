package com.geecee.escapelauncher.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import com.geecee.escapelauncher.core.common.DefaultSettings

object DefaultSettingsUi {
    val HOME_ALIGNMENT: Alignment.Horizontal = when (DefaultSettings.HOME_ALIGNMENT) {
        "Left" -> Alignment.Start
        "Center" -> Alignment.CenterHorizontally
        else -> Alignment.End
    }

    val HOME_V_ALIGNMENT: Arrangement.Vertical = when (DefaultSettings.HOME_V_ALIGNMENT) {
        "Top" -> Arrangement.Top
        "Center" -> Arrangement.Center
        else -> Arrangement.Bottom
    }

    val APPS_ALIGNMENT: Alignment.Horizontal = when (DefaultSettings.APPS_ALIGNMENT) {
        "Left" -> Alignment.Start
        "Center" -> Alignment.CenterHorizontally
        else -> Alignment.End
    }
}
