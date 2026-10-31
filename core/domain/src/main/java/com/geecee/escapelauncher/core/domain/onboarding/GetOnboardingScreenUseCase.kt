package com.geecee.escapelauncher.core.domain.onboarding

import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import jakarta.inject.Inject

/**
 * Represents the different screens available during the application's onboarding process.
 *
 * Each screen corresponds to a specific step or configuration task the user might need
 * to complete when first launching the app.
 */
enum class OnboardingScreen {
    WELCOME,
    STATISTICS,
    FAVORITES,
    DEFAULT_LAUNCHER,
    ANALYTICS,
    ACCESSIBILITY,
    FINISHED
}

/**
 * Use case responsible for determining the sequence of screens to be displayed during onboarding.
 *
 * The list of screens is dynamic and depends on factors such as the application flavor
 * (e.g., FOSS vs. non-FOSS) and the device's Android SDK version.
 *
 * @property appConfiguration The repository providing application-wide configuration settings.
 * @constructor Creates a [GetOnboardingScreenUseCase] with the required dependencies.
 * @see OnboardingScreen
 * @see AppConfiguration
 */
class GetOnboardingScreenUseCase @Inject constructor(
    private val appConfiguration: AppConfiguration,
) {
    /**
     * Executes the use case to retrieve the ordered list of onboarding screens.
     *
     * The logic for screen inclusion is as follows:
     * - [OnboardingScreen.WELCOME], [OnboardingScreen.STATISTICS], [OnboardingScreen.FAVORITES],
     *   and [OnboardingScreen.DEFAULT_LAUNCHER] are always included.
     * - [OnboardingScreen.ANALYTICS] is included only for non-FOSS builds.
     * - [OnboardingScreen.ACCESSIBILITY] is included for devices running Android P (API 28) or higher.
     * - [OnboardingScreen.FINISHED] is always included as the final step.
     *
     * @return A list of [OnboardingScreen] elements in the order they should be presented to the user.
     */
    operator fun invoke(): List<OnboardingScreen> {
        val isFoss = appConfiguration.isFoss
        val showAccessibility = appConfiguration.sdkInt >= MIN_SDK_ACCESSIBILITY

        val screens = listOfNotNull(
            OnboardingScreen.WELCOME,
            OnboardingScreen.STATISTICS,
            OnboardingScreen.FAVORITES,
            OnboardingScreen.DEFAULT_LAUNCHER,
            if (!isFoss) OnboardingScreen.ANALYTICS else null,
            if (showAccessibility) OnboardingScreen.ACCESSIBILITY else null,
            OnboardingScreen.FINISHED
        )

        return screens
    }

    private companion object {
        const val MIN_SDK_ACCESSIBILITY = 28 // Android 9 (Pie)
    }
}
