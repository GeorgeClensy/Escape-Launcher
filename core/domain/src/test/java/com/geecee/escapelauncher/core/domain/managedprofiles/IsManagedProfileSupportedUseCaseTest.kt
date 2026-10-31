package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.AppConfiguration
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsManagedProfileSupportedUseCaseTest {

    private val appConfiguration: AppConfiguration = mockk()
    private val useCase = IsManagedProfileSupportedUseCase(appConfiguration)

    @Test
    fun `private space is supported on android 15`() {
        every { appConfiguration.sdkInt } returns 35
        assertTrue(useCase(ManagedProfileType.PrivateSpace))
    }

    @Test
    fun `private space is not supported below android 15`() {
        every { appConfiguration.sdkInt } returns 34
        assertFalse(useCase(ManagedProfileType.PrivateSpace))
    }

    @Test
    fun `work apps are supported on android 11`() {
        every { appConfiguration.sdkInt } returns 30
        assertTrue(useCase(ManagedProfileType.WorkApps))
    }

    @Test
    fun `work apps are not supported below android 11`() {
        every { appConfiguration.sdkInt } returns 29
        assertFalse(useCase(ManagedProfileType.WorkApps))
    }
}
