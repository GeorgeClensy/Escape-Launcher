package com.geecee.escapelauncher.feature.settings.theme

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.setSolidColorWallpaperHomeScreen
import com.geecee.escapelauncher.core.theme.AppColourScheme
import com.geecee.escapelauncher.core.theme.ThemeViewModel
import com.geecee.escapelauncher.core.theme.resolveColorScheme
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor

/**
 * Theme options in settings
 *
 * @param goBack When back button is pressed
 *
 * @see Settings
 */
@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("MissingPermission")
@Composable
fun ThemeOptions(
    goBack: () -> Unit, themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val scheme by themeViewModel.theme.collectAsState()
    val lScheme by themeViewModel.ltheme.collectAsState()
    val dScheme by themeViewModel.dtheme.collectAsState()
    val syncTheme by themeViewModel.syncTheme.collectAsState(false)

    val isDark = isSystemInDarkTheme()
    var highlightedThemeId by remember { mutableIntStateOf(-1) }

    val themeIds = listOf(11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12)

    LazyColumn(Modifier.fillMaxSize()) {

        item {
            EscapeHeader(goBack, stringResource(R.string.theme))
        }

        item {
            SettingsSwitch(
                stringResource(R.string.syncLightDark),
                syncTheme,
                isTopOfGroup = true,
                onCheckedChange = {
                    highlightedThemeId = -1
                    themeViewModel.setSyncTheme(it)
                })
        }

        item {
            val activeTheme = if (syncTheme) {
                if (isDark) dScheme else lScheme
            } else scheme

            val colour = activeTheme.resolveColorScheme().background

            SettingsButton(
                label = stringResource(R.string.match_system_wallpaper),
                isBottomOfGroup = true,
                onClick = {
                    setSolidColorWallpaperHomeScreen(
                        context, colour.toAndroidColor()
                    )
                })
        }

        item { SettingsSpacer() }

        itemsIndexed(themeIds, key = { _, id -> id }) { index, themeId ->

            val isSelected = !syncTheme && scheme.id == themeId
            val isLight = syncTheme && lScheme.id == themeId
            val isDarkSel = syncTheme && dScheme.id == themeId
            val showPicker = highlightedThemeId == themeId

            ThemeCard(
                theme = themeId,

                showLightDarkPicker = showPicker,
                isSelected = isSelected,
                isLSelected = isLight,
                isDSelected = isDarkSel,

                updateLTheme = {
                    themeViewModel.setLTheme(AppColourScheme.fromId(themeId))
                    highlightedThemeId = -1
                },

                updateDTheme = {
                    themeViewModel.setDTheme(AppColourScheme.fromId(themeId))
                    highlightedThemeId = -1
                },

                modifier = Modifier.fillMaxWidth(),
                isTopOfGroup = index == 0,
                isBottomOfGroup = index == themeIds.size - 1,

                onClick = {
                    if (syncTheme) {
                        highlightedThemeId = themeId
                    } else {
                        themeViewModel.setTheme(AppColourScheme.fromId(themeId))
                    }
                })
        }

        item { SettingsSpacer() }
        item { SettingsSpacer() }
    }
}
