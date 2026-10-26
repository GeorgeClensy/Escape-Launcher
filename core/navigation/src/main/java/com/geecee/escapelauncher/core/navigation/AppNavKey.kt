package com.geecee.escapelauncher.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Top-level application destinations for Navigation 3.
 */
sealed interface AppNavKey : NavKey {
    @Serializable
    data object Home : AppNavKey

    @Serializable
    data object Settings : AppNavKey

    @Serializable
    data object Onboarding : AppNavKey
}