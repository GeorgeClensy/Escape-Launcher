package com.geecee.escapelauncher.feature.weather

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import jakarta.inject.Inject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class WeatherImpl @Inject constructor() : WeatherProxy {
    private val client = OkHttpClient()

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun getWeather(context: Context, useFarenheit: Boolean, callback: (String) -> Unit) {
        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        val priority = Priority.PRIORITY_HIGH_ACCURACY
        fusedLocationClient.getCurrentLocation(priority, CancellationTokenSource().token)
            .addOnSuccessListener { location ->
                Log.d("Weather", "Retrieved Weather Location: $location")

                if (location == null) {
                    callback("~~")
                    return@addOnSuccessListener
                }

                val lat = location.latitude
                val lon = location.longitude

                val unitParam = if (useFarenheit) "&temperature_unit=fahrenheit" else ""
                val unitSymbol = if (useFarenheit) "°F" else "°C"

                val url =
                    "https://api.open-meteo.com/v1/forecast?" +
                            "latitude=$lat&longitude=$lon&current_weather=true" +
                            unitParam


                val request = Request.Builder().url(url).build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("Weather", "Network request failed", e)
                        callback("~~")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        try {
                            response.body.string().let { json ->
                                val obj = JSONObject(json)
                                val weather = obj.optJSONObject("current_weather")
                                if (weather != null) {
                                    val temp = weather.optDouble("temperature", Double.NaN)
                                    callback("${temp.toInt()}${unitSymbol}")
                                } else {
                                    Log.e("Weather", "Weather data missing in response: $json")
                                    callback("No weather data")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Weather", "Error parsing weather data", e)
                            callback("~~")
                        }
                    }
                })
            }.addOnFailureListener { e ->
                Log.e("Weather", "Failed to get location", e)
                callback("unavailable")
            }
    }
}
