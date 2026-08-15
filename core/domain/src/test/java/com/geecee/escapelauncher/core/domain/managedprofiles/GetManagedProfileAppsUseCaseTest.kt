package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetManagedProfileAppsUseCaseTest {

    private lateinit var repository: ManagedProfileRepository
    private lateinit var useCase: GetManagedProfileAppsUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetManagedProfileAppsUseCase(repository)
    }

    @Test
    fun `invoke calls repository getApps with WorkApps type`() = runTest {
        // Given
        val apps = listOf(mockk<InstalledApp>())
        every { repository.getApps(ManagedProfileType.WorkApps) } returns flowOf(apps)

        // When
        val result = useCase(ManagedProfileType.WorkApps)

        // Then
        result.collect {
            assertEquals(apps, it)
        }
        verify { repository.getApps(ManagedProfileType.WorkApps) }
    }

    @Test
    fun `invoke calls repository getApps with PrivateSpace type`() = runTest {
        // Given
        val apps = listOf(mockk<InstalledApp>())
        every { repository.getApps(ManagedProfileType.PrivateSpace) } returns flowOf(apps)

        // When
        val result = useCase(ManagedProfileType.PrivateSpace)

        // Then
        result.collect {
            assertEquals(apps, it)
        }
        verify { repository.getApps(ManagedProfileType.PrivateSpace) }
    }
}
