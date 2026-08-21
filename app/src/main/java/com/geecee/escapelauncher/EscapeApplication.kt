package com.geecee.escapelauncher

import android.app.Application
import com.geecee.escapelauncher.feature.weather.WeatherImpl
import com.geecee.escapelauncher.feature.weather.weatherProxy
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EscapeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize flavor-specific proxies
        weatherProxy = WeatherImpl()
    }
}
