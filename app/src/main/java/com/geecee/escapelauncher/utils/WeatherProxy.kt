package com.geecee.escapelauncher.utils

import android.content.Context

interface WeatherProxy {
    fun getWeather(context: Context, useFarenheit: Boolean, callback: (String) -> Unit)
}

// Global accessor that will be provided by the flavor-specific implementations
lateinit var weatherProxy: WeatherProxy