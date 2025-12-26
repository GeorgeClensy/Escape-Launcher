package com.geecee.escapelauncher.utils

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class WeatherImpl : WeatherProxy {
    private val client = OkHttpClient()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getWeather(context: Context, callback: (String) -> Unit) {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            Log.d("Weather","Retrieved Weather")


            if (location == null) {
                callback("Location unavailable")
                return@addOnSuccessListener
            }

            val lat = location.latitude
            val lon = location.longitude

            val url =
                "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=$lat&longitude=$lon&current_weather=true"

            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback("Weather error")
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.string()?.let { json ->

                        val obj = JSONObject(json)
                        val weather = obj.optJSONObject("current_weather")
                        if (weather != null) {
                            val temp = weather.optDouble("temperature", Double.NaN)
                            callback("${temp.toInt()}°C")
                        } else {
                            callback("No weather data")
                        }
                    }
                }
            })
        }
    }
}
