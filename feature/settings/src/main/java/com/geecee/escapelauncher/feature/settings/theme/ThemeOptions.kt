package com.geecee.escapelauncher.feature.settings.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.theme.colours.AppColourScheme
import com.geecee.escapelauncher.core.theme.ThemeViewModel
import com.geecee.escapelauncher.core.theme.colours.resolveColorScheme
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor

/**
 * Theme options in settings
 *
 * @param goBack When back button is pressed
 */
@SuppressLint("MissingPermission")
@Composable
fun ThemeOptions(
    goBack: () -> Unit, themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val scheme by themeViewModel.theme.collectAsState()
    val selectableThemes = AppColourScheme.selectableThemes

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

            item {
                EscapeHeader(goBack, stringResource(R.string.theme))
            }

            item {
                val activeTheme = scheme
                val colour = activeTheme.resolveColorScheme().background

                SettingsButton(
                    label = stringResource(R.string.match_system_wallpaper),
                    isTopOfGroup = true,
                    isBottomOfGroup = true,
                    onClick = {
                        themeViewModel.setWallpaper(colour.toAndroidColor())
                    })
            }

            item { SettingsSpacer() }

            itemsIndexed(selectableThemes, key = { _, theme -> theme.id }) { index, themeOption ->
                val isSelected = scheme == themeOption

                ThemeCard(
                    scheme = themeOption,
                    isSelected = isSelected,
                    modifier = Modifier.fillMaxWidth(),
                    isTopOfGroup = index == 0,
                    isBottomOfGroup = index == selectableThemes.size - 1,
                    onClick = {
                        themeViewModel.setTheme(themeOption)
                    })
            }

            item { SettingsSpacer() }
            item { SettingsSpacer() }
        }
    }
}