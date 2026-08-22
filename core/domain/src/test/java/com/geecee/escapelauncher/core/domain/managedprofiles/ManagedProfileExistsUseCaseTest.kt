package com.geecee.escapelauncher.core.domain.managedprofiles

import com.geecee.escapelauncher.core.domain.repository.android.ManagedProfileRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ManagedProfileExistsUseCaseTest {

    private val repository: ManagedProfileRepository = mockk()
    private val useCase = ManagedProfileExistsUseCase(repository)

    @Test
    fun `returns true when repository says profile exists`() {
        every { repository.exists(ManagedProfileType.WorkApps) } returns true
        assertTrue(useCase(ManagedProfileType.WorkApps))
    }

    @Test
    fun `returns false when repository says profile does not exist`() {
        every { repository.exists(ManagedProfileType.PrivateSpace) } returns false
        assertFalse(useCase(ManagedProfileType.PrivateSpace))
    }
}
