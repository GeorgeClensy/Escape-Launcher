package com.geecee.escapelauncher.core.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferencesKeys {
    val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")

    //Private Space
    val HIDE_PRIVATE_SPACE = booleanPreferencesKey("hidden_private_space")
}