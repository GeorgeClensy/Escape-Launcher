package com.geecee.escapelauncher.feature.weather

import android.content.Context

class WeatherImpl : WeatherProxy {
    override fun getWeather(context: Context, useFarenheit: Boolean, callback: (String) -> Unit) {
        callback("Weather is not available on the FOSS version of the app")
    }
}
