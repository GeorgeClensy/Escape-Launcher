package com.geecee.escapelauncher.core.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferencesKeys {
    val HAPTIC_FEEDBACK = booleanPreferencesKey(name = "haptic_feedback")

    //Private Space
    val HIDE_PRIVATE_SPACE = booleanPreferencesKey(name = "hidden_private_space")

    //Home Screen
    val TWELVE_HOUR_CLOCK = booleanPreferencesKey(name = "twelve_hour_clock")
    val SHOW_CLOCK = booleanPreferencesKey(name = "show_clock")
    val BIG_CLOCK = booleanPreferencesKey(name = "big_clock")
    val SHOW_DATE = booleanPreferencesKey(name = "show_date")
    val SHOW_SCREEN_TIME_HOME = booleanPreferencesKey(name = "show_screen_time_home")
    val SHOW_WEATHER = booleanPreferencesKey(name = "show_weather")
    val SHOW_SCREEN_TIME_APP = booleanPreferencesKey(name = "show_screen_time_app")
    val FIRST_TIME_HELP = booleanPreferencesKey(name = "first_time_help")
}