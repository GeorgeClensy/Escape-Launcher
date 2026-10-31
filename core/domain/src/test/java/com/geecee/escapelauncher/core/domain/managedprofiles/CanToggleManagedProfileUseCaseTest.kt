package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanToggleManagedProfileUseCaseTest {

    private val appConfiguration: AppConfiguration = mockk()
    private val useCase = CanToggleManagedProfileUseCase(appConfiguration)

    @Test
    fun `private space can be toggled on android 15`() {
        every { appConfiguration.sdkInt } returns 35
        assertTrue(useCase(ManagedProfileType.PrivateSpace))
    }

    @Test
    fun `private space cannot be toggled below android 15`() {
        every { appConfiguration.sdkInt } returns 34
        assertFalse(useCase(ManagedProfileType.PrivateSpace))
    }

    @Test
    fun `work apps can be toggled on android 9`() {
        every { appConfiguration.sdkInt } returns 28
        assertTrue(useCase(ManagedProfileType.WorkApps))
    }

    @Test
    fun `work apps cannot be toggled below android 9`() {
        every { appConfiguration.sdkInt } returns 27
        assertFalse(useCase(ManagedProfileType.WorkApps))
    }
}
