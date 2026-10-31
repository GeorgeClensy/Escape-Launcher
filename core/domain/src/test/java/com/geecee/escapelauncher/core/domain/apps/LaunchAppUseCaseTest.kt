package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.domain.repository.android.SystemActionsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LaunchAppUseCaseTest {

    private val systemActionsRepository: SystemActionsRepository = mockk()
    private val analyticsProxy: AnalyticsProxy = mockk(relaxed = true)
    private val useCase = LaunchAppUseCase(systemActionsRepository, analyticsProxy)

    private val app = mockk<InstalledApp> {
        every { packageName } returns "com.example.app"
    }

    @Test
    fun `returns true and notifies the callback when the launch succeeds`() {
        // Given
        every { systemActionsRepository.launchApp(app) } just runs
        var openedPackage: String? = null

        // When
        val result = useCase(app) { openedPackage = it }

        // Then
        assertTrue(result)
        assertEquals("com.example.app", openedPackage)
        verify(exactly = 1) { systemActionsRepository.launchApp(app) }
        verify(exactly = 0) { analyticsProxy.logCustomKey(any(), any()) }
        verify(exactly = 0) { analyticsProxy.recordException(any()) }
    }

    @Test
    fun `returns true when the launch succeeds without a callback`() {
        // Given
        every { systemActionsRepository.launchApp(app) } just runs

        // When
        val result = useCase(app)

        // Then
        assertTrue(result)
    }

    @Test
    fun `returns false, skips the callback and records analytics when the launch fails`() {
        // Given
        val failure = SecurityException("launch denied")
        every { systemActionsRepository.launchApp(app) } throws failure
        var callbackInvoked = false

        // When
        val result = useCase(app) { callbackInvoked = true }

        // Then
        assertFalse(result)
        assertFalse(callbackInvoked)
        verify(exactly = 1) { analyticsProxy.logCustomKey("app_launch_error", "com.example.app") }
        verify(exactly = 1) { analyticsProxy.recordException(failure) }
    }
}
