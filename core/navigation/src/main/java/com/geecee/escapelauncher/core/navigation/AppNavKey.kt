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
    data object Onboarding : AppNavKey

    sealed interface Settings : AppNavKey {
        @Serializable
        data object MainSettingsPage : AppNavKey

        @Serializable
        data object HiddenApps : AppNavKey

        @Serializable
        data object OpenChallenges : AppNavKey

        @Serializable
        data object ChooseFont : AppNavKey

        @Serializable
        data object DevOptions : AppNavKey

        @Serializable
        data object Theme : AppNavKey

        @Serializable
        data object Widget : AppNavKey

        @Serializable
        data object BulkHiddenApps : AppNavKey

        @Serializable
        data object BulkFavouriteApps : AppNavKey

        @Serializable
        data object FontLicences : AppNavKey

        @Serializable
        data object PrivacyPolicy : AppNavKey
    }
}