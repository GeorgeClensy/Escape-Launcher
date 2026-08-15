package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ObserveManagedProfileUnlockedUseCaseTest {

    private lateinit var repository: ManagedProfileRepository
    private lateinit var useCase: ObserveManagedProfileUnlockedUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = ObserveManagedProfileUnlockedUseCase(repository)
    }

    @Test
    fun `invoke calls repository observeUnlocked with WorkApps type`() = runTest {
        // Given
        every { repository.observeUnlocked(ManagedProfileType.WorkApps) } returns flowOf(true)

        // When
        val result = useCase(ManagedProfileType.WorkApps)

        // Then
        result.collect {
            assertEquals(true, it)
        }
        verify { repository.observeUnlocked(ManagedProfileType.WorkApps) }
    }

    @Test
    fun `invoke calls repository observeUnlocked with PrivateSpace type`() = runTest {
        // Given
        every { repository.observeUnlocked(ManagedProfileType.PrivateSpace) } returns flowOf(false)

        // When
        val result = useCase(ManagedProfileType.PrivateSpace)

        // Then
        result.collect {
            assertEquals(false, it)
        }
        verify { repository.observeUnlocked(ManagedProfileType.PrivateSpace) }
    }
}
