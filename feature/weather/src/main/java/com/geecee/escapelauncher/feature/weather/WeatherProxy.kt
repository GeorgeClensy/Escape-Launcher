package com.geecee.escapelauncher.feature.weather

import android.content.Context

interface WeatherProxy {
    fun getWeather(context: Context, useFarenheit: Boolean, callback: (String) -> Unit)
}