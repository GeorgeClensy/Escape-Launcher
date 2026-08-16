package com.geecee.escapelauncher.core.domain.onboarding

import android.os.Build
import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetOnboardingScreenUseCaseTest {

    private val appConfiguration: AppConfiguration = mockk()
    private lateinit var useCase: GetOnboardingScreenUseCase

    @Before
    fun setup() {
        // Default setup
        every { appConfiguration.isFoss } returns false
    }

    @Test
    fun `should show all screens when not FOSS and SDK is P or higher`() {
        useCase = GetOnboardingScreenUseCase(appConfiguration, Build.VERSION_CODES.P)
        val screens = useCase()

        assertTrue(screens.contains(OnboardingScreen.WELCOME))
        assertTrue(screens.contains(OnboardingScreen.STATISTICS))
        assertTrue(screens.contains(OnboardingScreen.FAVORITES))
        assertTrue(screens.contains(OnboardingScreen.DEFAULT_LAUNCHER))
        assertTrue(screens.contains(OnboardingScreen.ANALYTICS))
        assertTrue(screens.contains(OnboardingScreen.ACCESSIBILITY))
        assertTrue(screens.contains(OnboardingScreen.FINISHED))
    }

    @Test
    fun `should omit ANALYTICS screen when build is FOSS`() {
        every { appConfiguration.isFoss } returns true
        useCase = GetOnboardingScreenUseCase(appConfiguration, Build.VERSION_CODES.P)
        val screens = useCase()

        assertFalse(screens.contains(OnboardingScreen.ANALYTICS))
        assertTrue(screens.contains(OnboardingScreen.ACCESSIBILITY))
    }

    @Test
    fun `should omit ACCESSIBILITY screen when SDK is lower than P`() {
        useCase = GetOnboardingScreenUseCase(appConfiguration, Build.VERSION_CODES.O)
        val screens = useCase()

        assertFalse(screens.contains(OnboardingScreen.ACCESSIBILITY))
        assertTrue(screens.contains(OnboardingScreen.ANALYTICS))
    }

    @Test
    fun `should omit both ANALYTICS and ACCESSIBILITY when FOSS and SDK is lower than P`() {
        every { appConfiguration.isFoss } returns true
        useCase = GetOnboardingScreenUseCase(appConfiguration, Build.VERSION_CODES.O)
        val screens = useCase()

        assertFalse(screens.contains(OnboardingScreen.ANALYTICS))
        assertFalse(screens.contains(OnboardingScreen.ACCESSIBILITY))
        
        // Verify mandatory screens still present
        assertTrue(screens.contains(OnboardingScreen.WELCOME))
        assertTrue(screens.contains(OnboardingScreen.FINISHED))
    }

    @Test
    fun `should always contain mandatory screens`() {
        useCase = GetOnboardingScreenUseCase(appConfiguration)
        val screens = useCase()

        val mandatoryScreens = listOf(
            OnboardingScreen.WELCOME,
            OnboardingScreen.STATISTICS,
            OnboardingScreen.FAVORITES,
            OnboardingScreen.DEFAULT_LAUNCHER,
            OnboardingScreen.FINISHED,
        )

        assertTrue(screens.containsAll(mandatoryScreens))
    }
}
