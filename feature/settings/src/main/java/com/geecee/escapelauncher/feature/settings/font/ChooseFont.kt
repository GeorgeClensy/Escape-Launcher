package com.geecee.escapelauncher.feature.settings.font

import android.content.Context
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.geecee.escapelauncher.core.ui.R
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.theme.ThemeViewModel
import com.geecee.escapelauncher.core.theme.type.getFontFamily
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer


/**
 * Font options in settings
 *
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChooseFont(
    context: Context,
    themeViewModel: ThemeViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    val selectedFont by themeViewModel.font.collectAsState(initial = DefaultSettings.FONT)

    val fontNames = listOf(
        "Jost",
        "Inter",
        "Lexend",
        "Work Sans",
        "Poppins",
        "Roboto",
        "Open Sans",
        "Lora",
        "Outfit",
        "IBM Plex Sans",
        "IBM Plex Serif"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            item { EscapeHeader(goBack, stringResource(R.string.font)) }

            itemsIndexed(fontNames) { index, fontName ->
                SettingsButton(
                    label = fontName,
                    onClick = {
                        themeViewModel.setFont(fontName)
                    },
                    isTopOfGroup = index == 0,
                    isBottomOfGroup = index == fontNames.lastIndex,
                    fontFamily = getFontFamily(context, fontName),
                    isSelected = fontName == selectedFont
                )
            }

            item { SettingsSpacer() }
            item { SettingsSpacer() }
        }
    }
}