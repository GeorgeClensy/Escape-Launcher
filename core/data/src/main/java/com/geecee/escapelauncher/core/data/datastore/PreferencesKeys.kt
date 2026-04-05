package com.geecee.escapelauncher.core.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferencesKeys {
    val HAPTIC_FEEDBACK = booleanPreferencesKey(name = "haptic_feedback")

    //Private Space
    val HIDE_PRIVATE_SPACE = booleanPreferencesKey(name = "hidden_private_space")

    //Home Screen
    val TWELVE_HOUR_CLOCK = booleanPreferencesKey(name = "twelve_hour_clock")
}