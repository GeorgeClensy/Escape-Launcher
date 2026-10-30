package com.geecee.escapelauncher.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.DefaultSettings
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.GlanceWidget
import com.geecee.escapelauncher.feature.weather.WeatherViewModel

@Composable
fun HomeWeatherImpl(
    alignment: Alignment.Horizontal,
    color: Color = MaterialTheme.colorScheme.primary,
    shadow: Boolean = false,
    weatherViewModel: WeatherViewModel = hiltViewModel(LocalActivity.current as ComponentActivity)
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val weatherAppPackage by weatherViewModel.weatherAppPackage.collectAsState(initial = DefaultSettings.WEATHER_APP_PACKAGE)

    AnimatedVisibility(
        weatherViewModel.weatherText.value != "",
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        GlanceWidget(
            text = weatherViewModel.weatherText.value,
            icon = Icons.Default.WbSunny,
            iconContentDescription = "Weather",
            shadow = shadow,
            homeAlignment = alignment,
            small = true,
            color = color,
            onClick = {
                if (weatherAppPackage.isNotEmpty()) {
                    //todo: use OpenApp() here so it's tracked

                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(
                            weatherAppPackage
                        )
                    launchIntent?.let {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(it)
                    }
                } else {
                    Toast.makeText(
                        context,
                        resources.getString(R.string.set_weather_app_in_settings),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}
