package com.geecee.escapelauncher.feature.settings.devoptions

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.ui.composables.EscapeHeader
import com.geecee.escapelauncher.core.ui.composables.SettingsButton
import com.geecee.escapelauncher.core.ui.composables.SettingsSwitch
import kotlin.system.exitProcess

/**
 * Developer options in settings
 */
@Composable
fun DevOptions(
    viewModel: DevOptionsPageViewModel = hiltViewModel(),
    goBack: () -> Unit
) {
    val context = LocalContext.current
    val firstTimeHelp by viewModel.firstTimeHelp.collectAsState(initial = DefaultSettings.FIRST_TIME_HELP)
    val firstTime by viewModel.firstTime.collectAsState(initial = DefaultSettings.FIRST_TIME)
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsState(initial = DefaultSettings.DOUBLE_TAP_TO_LOCK)

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
            item(key = "header") { EscapeHeader(goBack, "Developer Options") }

            item(key = "first_time") {
                SettingsSwitch(
                    "First time",
                    firstTimeHelp && firstTime,
                    onCheckedChange = {
                        viewModel.setFirstTimeHelp(it)
                        viewModel.setFirstTime(it)
                    },
                    isTopOfGroup = true
                )
            }

            item(key = "force_stop") {
                SettingsButton(
                    label = "Force Stop",
                    onClick = {
                        exitProcess(0)
                    }
                )
            }

            item(key = "clear_weather") {
                SettingsButton(
                    label = "Clear weather app",
                    onClick = {
                        viewModel.setWeatherAppPackage("")
                        Toast.makeText(context, "Weather app cleared", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            item(key = "force_crash") {
                SettingsButton(
                    label = "Force crash",
                    onClick = {
                        throw RuntimeException("Test Crash")
                    }
                )
            }

            item(key = "test_screen_off") {
                SettingsButton(
                    label = "Test Screen Off",
                    isBottomOfGroup = true,
                    onClick = {
                        if (doubleTapToLock) {
                            viewModel.lockScreen()
                        }
                    }
                )
            }
        }
    }
}