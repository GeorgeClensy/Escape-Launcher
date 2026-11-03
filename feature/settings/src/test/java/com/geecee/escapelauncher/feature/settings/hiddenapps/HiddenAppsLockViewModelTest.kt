package com.geecee.escapelauncher.feature.settings.hiddenapps

import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HiddenAppsLockViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val configured = MutableStateFlow(true)
    private val repository = mockk<HiddenAppsPasswordRepository>()
    private lateinit var viewModel: HiddenAppsLockViewModel

    @Before fun setup() {
        Dispatchers.setMain(dispatcher)
        every { repository.isPasswordSet } returns configured
        every { repository.recoveryMinutes } returns MutableStateFlow(1)
        viewModel = HiddenAppsLockViewModel(repository)
    }

    @After fun cleanup() {
        viewModel.lock()
        Dispatchers.resetMain()
    }

    @Test fun requiresPasswordAndRelocksAfterSuccessfulAuthentication() = runTest(dispatcher) {
        assertFalse(viewModel.state.value.canAccess)
        runCurrent()
        coEvery { repository.verifyPassword("secret") } returns true
        viewModel.unlock("secret")
        runCurrent()
        assertTrue(viewModel.state.value.canAccess)
        viewModel.lock()
        assertFalse(viewModel.state.value.canAccess)
    }

    @Test fun wrongPasswordStaysLockedAndSerializesAttempts() = runTest(dispatcher) {
        runCurrent()
        coEvery { repository.verifyPassword(any()) } returns false
        viewModel.unlock("wrong")
        runCurrent()
        viewModel.unlock("another")
        assertTrue(viewModel.state.value.isBusy)
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(viewModel.state.value.canAccess)
        assertEquals(HiddenAppsLockError.IncorrectPassword, viewModel.state.value.error)
        coVerify(exactly = 1) { repository.verifyPassword(any()) }
    }

    @Test fun leavingDuringVerificationCannotUnlockLater() = runTest(dispatcher) {
        runCurrent()
        coEvery { repository.verifyPassword(any()) } coAnswers { delay(500); true }
        viewModel.unlock("secret")
        runCurrent()
        viewModel.lock()
        advanceTimeBy(1000)
        runCurrent()
        assertFalse(viewModel.state.value.canAccess)
        assertFalse(viewModel.state.value.isBusy)
    }

    @Test fun settingPasswordLocksImmediatelyAndLockedAreaCannotChangeIt() = runTest(dispatcher) {
        configured.value = false
        runCurrent()
        coEvery { repository.updatePassword("", "secret") } coAnswers {
            configured.value = true
            true
        }
        var saved = false
        viewModel.updatePassword("", "secret") { saved = true }
        runCurrent()
        assertTrue(saved)
        assertFalse(viewModel.state.value.canAccess)
        viewModel.updatePassword("", null) {}
        runCurrent()
        coVerify(exactly = 1) { repository.updatePassword(any(), any()) }
    }
    @Test fun recoveryCannotFinishEarlyAndLeavingResetsProgress() = runTest(dispatcher) {
        runCurrent()
        viewModel.startRecovery()
        assertEquals(60, viewModel.state.value.recoverySecondsRemaining)
        advanceTimeBy(30_000)
        runCurrent()
        assertEquals(30, viewModel.state.value.recoverySecondsRemaining)
        viewModel.finishRecovery()
        runCurrent()
        coVerify(exactly = 0) { repository.removePasswordAfterRecovery() }
        viewModel.lock()
        assertNull(viewModel.state.value.recoverySecondsRemaining)
        advanceTimeBy(60_000)
        runCurrent()
        assertFalse(viewModel.state.value.canAccess)
        viewModel.startRecovery()
        assertEquals(60, viewModel.state.value.recoverySecondsRemaining)
        viewModel.cancelRecovery()
    }

    @Test fun completedRecoveryRequiresExplicitConfirmation() = runTest(dispatcher) {
        coEvery { repository.removePasswordAfterRecovery() } coAnswers { configured.value = false }
        runCurrent()
        viewModel.startRecovery()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, viewModel.state.value.recoverySecondsRemaining)
        assertFalse(viewModel.state.value.canAccess)
        coVerify(exactly = 0) { repository.removePasswordAfterRecovery() }
        viewModel.finishRecovery()
        runCurrent()
        assertTrue(viewModel.state.value.canAccess)
        assertNull(viewModel.state.value.recoverySecondsRemaining)
        coVerify(exactly = 1) { repository.removePasswordAfterRecovery() }
    }

    @Test fun recoveryDurationCannotBeChangedFromLockedScreen() = runTest(dispatcher) {
        runCurrent()
        viewModel.updateRecoveryMinutes("", 1) {}
        runCurrent()
        coVerify(exactly = 0) { repository.updateRecoveryMinutes(any(), any()) }
    }
    @Test fun repeatedStartCannotAccelerateRecoveryOrFinishOneMillisecondEarly() = runTest(dispatcher) {
        runCurrent()
        viewModel.startRecovery()
        viewModel.startRecovery()
        advanceTimeBy(59_999)
        runCurrent()
        assertEquals(1, viewModel.state.value.recoverySecondsRemaining)
        viewModel.finishRecovery()
        runCurrent()
        coVerify(exactly = 0) { repository.removePasswordAfterRecovery() }
        advanceTimeBy(1)
        runCurrent()
        assertEquals(0, viewModel.state.value.recoverySecondsRemaining)
    }

    @Test fun leavingAfterZeroInvalidatesCompletionAndRequiresFullWaitAgain() = runTest(dispatcher) {
        runCurrent()
        viewModel.startRecovery()
        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(0, viewModel.state.value.recoverySecondsRemaining)
        viewModel.lock()
        viewModel.finishRecovery()
        runCurrent()
        coVerify(exactly = 0) { repository.removePasswordAfterRecovery() }
        assertFalse(viewModel.state.value.canAccess)
        viewModel.startRecovery()
        assertEquals(60, viewModel.state.value.recoverySecondsRemaining)
    }

    @Test fun recreatedViewModelDoesNotInheritRecoveryOrUnlockState() = runTest(dispatcher) {
        runCurrent()
        viewModel.startRecovery()
        advanceTimeBy(60_000)
        runCurrent()
        val recreated = HiddenAppsLockViewModel(repository)
        runCurrent()
        assertFalse(recreated.state.value.canAccess)
        assertNull(recreated.state.value.recoverySecondsRemaining)
        recreated.finishRecovery()
        runCurrent()
        coVerify(exactly = 0) { repository.removePasswordAfterRecovery() }
        recreated.lock()
    }

    @Test fun verificationStorageFailureNeverUnlocks() = runTest(dispatcher) {
        runCurrent()
        coEvery { repository.verifyPassword(any()) } throws java.io.IOException("unavailable")
        viewModel.unlock("secret")
        runCurrent()
        assertFalse(viewModel.state.value.canAccess)
        assertFalse(viewModel.state.value.isBusy)
        assertEquals(HiddenAppsLockError.StorageError, viewModel.state.value.error)
    }

    @Test fun recoveryStorageFailureDoesNotGrantAccessAndCanRetry() = runTest(dispatcher) {
        runCurrent()
        coEvery { repository.removePasswordAfterRecovery() } throws java.io.IOException("unavailable")
        viewModel.startRecovery()
        advanceTimeBy(60_000)
        runCurrent()
        viewModel.finishRecovery()
        runCurrent()
        assertFalse(viewModel.state.value.canAccess)
        assertFalse(viewModel.state.value.isBusy)
        assertEquals(0, viewModel.state.value.recoverySecondsRemaining)
        assertEquals(HiddenAppsLockError.StorageError, viewModel.state.value.error)
        coEvery { repository.removePasswordAfterRecovery() } coAnswers { configured.value = false }
        viewModel.finishRecovery()
        runCurrent()
        assertTrue(viewModel.state.value.canAccess)
    }
}
