package com.geecee.escapelauncher.feature.settings.theme

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings.NIGHT_MODE_DARK
import com.geecee.escapelauncher.core.common.DefaultSettings.NIGHT_MODE_SYNC
import com.geecee.escapelauncher.core.theme.colours.AppColourScheme
import com.geecee.escapelauncher.core.theme.ThemeViewModel
import com.geecee.escapelauncher.core.theme.colours.resolveColorScheme
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.EscapeSubhead
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSingleChoiceSegmentedButtons
import com.geecee.escapelauncher.core.ui.composables.SettingsSmallSpacer
import com.geecee.escapelauncher.core.ui.composables.SettingsSpacer
import com.geecee.escapelauncher.core.ui.utils.toAndroidColor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

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
    val modeChanged = remember { mutableStateOf(false) }
    val scheme by themeViewModel.theme.collectAsState()
    val showWallpaper by themeViewModel.showWallpaper.collectAsState(initial = false)
    val blackBackground by themeViewModel.blackBackground.collectAsState(initial = false)
    val nightMode by themeViewModel.nightMode.collectAsState(initial = NIGHT_MODE_SYNC)

    val selectableThemes = remember(showWallpaper) {
        AppColourScheme.selectableThemes.filter { themeOption ->
            when (themeOption) {
                AppColourScheme.MONOCHROME -> !showWallpaper
                AppColourScheme.WALLPAPER -> showWallpaper && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
                AppColourScheme.SYSTEM -> Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !showWallpaper
                else -> true
            }
        }
    }

    LaunchedEffect(showWallpaper, scheme, selectableThemes) {
        if (modeChanged.value) {
            if (showWallpaper && selectableThemes.contains(AppColourScheme.WALLPAPER)) {
                coroutineScope {
                    delay(200.milliseconds)
                    themeViewModel.setTheme(AppColourScheme.WALLPAPER)
                }
            } else if (!selectableThemes.contains(scheme)) {
                coroutineScope {
                    delay(200.milliseconds)
                    themeViewModel.setTheme(AppColourScheme.ESCAPE_THEME)
                }
            }

            modeChanged.value = false
        }
    }

    val backgroundSelectedIndex = when {
        showWallpaper -> 2
        blackBackground -> 1
        else -> 0
    }

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

            item(key = "header") {
                EscapeHeader(goBack, stringResource(R.string.theme))
            }

            item(key = "background_header") {
                EscapeSubhead(stringResource(R.string.background))
            }

            item(key = "background_segmented_button") {
                val backgroundOptions = listOf(
                    stringResource(R.string.colorful),
                    stringResource(R.string.pitch_black),
                    stringResource(R.string.wallpaper)
                )

                SettingsSingleChoiceSegmentedButtons(
                    label = "",
                    options = backgroundOptions,
                    selectedIndex = backgroundSelectedIndex,
                    onSelectedIndexChange = { index ->
                        @Suppress(
                            "UnnecessaryVariable",
                            "RedundantSuppression"
                        ) // This is created so we can get the value before it changes, for some reason Android Studio flags it.
                        val preChangeIndex = backgroundSelectedIndex

                        when (index) {
                            0 -> themeViewModel.setBackgroundMode(
                                showWallpaper = false, blackBackground = false
                            ) // Colorful
                            1 -> themeViewModel.setBackgroundMode(
                                showWallpaper = false, blackBackground = true
                            )  // Pitch Black
                            2 -> themeViewModel.setBackgroundMode(
                                showWallpaper = true, blackBackground = false
                            )  // Wallpaper
                        }

                        if (index != preChangeIndex) { // This is just so if you tap wallpaper when on wallpaper and change the theme, it doesn't get set back to wallpaper theme for example
                            modeChanged.value = true
                        }
                    },
                    isTopOfGroup = true,
                    isBottomOfGroup = showWallpaper
                )
            }

            item(key = "match_wallpaper_button") {
                AnimatedVisibility(
                    visible = !showWallpaper,
                    enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                    exit = fadeOut(tween(300)) + shrinkVertically(tween(300))
                ) {
                    val activeTheme = scheme
                    val colour = activeTheme.resolveColorScheme().background

                    SettingsButton(
                        label = stringResource(R.string.match_system_wallpaper),
                        isTopOfGroup = false,
                        isBottomOfGroup = true,
                        onClick = {
                            themeViewModel.setWallpaper(colour.toAndroidColor())
                        }
                    )
                }
            }

            item(key = "colours_header") {
                EscapeSubhead(stringResource(R.string.colours))
            }

            item(key = "nightmode_segmented_button") {
                val nightModeOptions = listOf(
                    stringResource(R.string.system),
                    stringResource(R.string.dark),
                    stringResource(R.string.light)
                )

                SettingsSingleChoiceSegmentedButtons(
                    label = "",
                    options = nightModeOptions,
                    selectedIndex = if(blackBackground) NIGHT_MODE_DARK else nightMode,
                    onSelectedIndexChange = { index ->
                        themeViewModel.setNightMode(index)
                    },
                    isTopOfGroup = true,
                    isBottomOfGroup = true,
                    enabled = !blackBackground
                )
            }

            item {
                SettingsSmallSpacer()
            }

            itemsIndexed(selectableThemes, key = { _, theme -> theme.id }) { index, themeOption ->
                val isSelected = scheme == themeOption

                ThemeCard(
                    scheme = themeOption,
                    isSelected = isSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem(),
                    isTopOfGroup = index == 0,
                    isBottomOfGroup = index == selectableThemes.size - 1,
                    onClick = {
                        themeViewModel.setTheme(themeOption)
                    },
                    isDark = when (nightMode) {
                        0 -> isSystemInDarkTheme()
                        1 -> true
                        else -> false
                    }
                )
            }

            item(key = "spacer_bottom1") { SettingsSpacer() }
            item(key = "spacer_bottom2") { SettingsSpacer() }
        }
    }
}