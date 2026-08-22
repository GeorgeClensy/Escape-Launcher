package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleManagedProfileUseCaseTest {

    private val repository: ManagedProfileRepository = mockk()
    private val useCase = ToggleManagedProfileUseCase(repository)

    @Test
    fun `when launcher is not default, return FailedNotDefaultLauncher`() = runTest {
        every { repository.isDefaultLauncher() } returns false

        val result = useCase(ManagedProfileType.WorkApps)

        assertEquals(ToggleManagedProfileUseCaseOutput.FailedNotDefaultLauncher, result)
    }

    @Test
    fun `when profile is unlocked, lock it and return SuccessfulToggleOff`() = runTest {
        every { repository.isDefaultLauncher() } returns true
        every { repository.isUnlocked(ManagedProfileType.WorkApps) } returns true
        coEvery { repository.lock(ManagedProfileType.WorkApps) } returns Unit

        val result = useCase(ManagedProfileType.WorkApps)

        assertEquals(ToggleManagedProfileUseCaseOutput.SuccessfulToggleOff, result)
        coVerify { repository.lock(ManagedProfileType.WorkApps) }
    }

    @Test
    fun `when profile is locked, unlock it and return SuccessfulToggleOn`() = runTest {
        every { repository.isDefaultLauncher() } returns true
        every { repository.isUnlocked(ManagedProfileType.WorkApps) } returns false
        coEvery { repository.unlock(ManagedProfileType.WorkApps) } returns Unit

        val result = useCase(ManagedProfileType.WorkApps)

        assertEquals(ToggleManagedProfileUseCaseOutput.SuccessfulToggleOn, result)
        coVerify { repository.unlock(ManagedProfileType.WorkApps) }
    }
}
