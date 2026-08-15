package com.geecee.escapelauncher.feature.workapps

import com.geecee.escapelauncher.core.domain.managedprofiles.CanToggleManagedProfileUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.GetManagedProfileAppsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.managedprofiles.ObserveManagedProfileUnlockedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCaseOutput
import com.geecee.escapelauncher.core.model.InstalledApp
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkAppsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getManagedProfileAppsUseCase: GetManagedProfileAppsUseCase = mockk()
    private val observeManagedProfileUnlockedUseCase: ObserveManagedProfileUnlockedUseCase = mockk()
    private val toggleManagedProfileUseCase: ToggleManagedProfileUseCase = mockk()
    private val canToggleManagedProfileUseCase: CanToggleManagedProfileUseCase = mockk()

    private lateinit var viewModel: WorkAppsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        // Default mocks for initialization
        every { canToggleManagedProfileUseCase(ManagedProfileType.WorkApps) } returns true
        every { observeManagedProfileUnlockedUseCase(ManagedProfileType.WorkApps) } returns flowOf(true)
        every { getManagedProfileAppsUseCase(ManagedProfileType.WorkApps) } returns flowOf(emptyList())
        
        viewModel = WorkAppsViewModel(
            getManagedProfileAppsUseCase,
            observeManagedProfileUnlockedUseCase,
            toggleManagedProfileUseCase,
            canToggleManagedProfileUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `canToggleProfile reflects use case result`() {
        assertEquals(true, viewModel.canToggleProfile)
        verify { canToggleManagedProfileUseCase(ManagedProfileType.WorkApps) }
    }

    @Test
    fun `isUnlocked reflects observeManagedProfileUnlockedUseCase`() = runTest {
        // Given
        val unlockedFlow = MutableStateFlow(false)
        every { observeManagedProfileUnlockedUseCase(ManagedProfileType.WorkApps) } returns unlockedFlow
        
        // Re-init to pick up the new flow
        viewModel = WorkAppsViewModel(
            getManagedProfileAppsUseCase,
            observeManagedProfileUnlockedUseCase,
            toggleManagedProfileUseCase,
            canToggleManagedProfileUseCase
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.isUnlocked.collect {} }

        // When
        unlockedFlow.value = true

        // Then
        assertEquals(true, viewModel.isUnlocked.value)
    }

    @Test
    fun `workApps reflects getManagedProfileAppsUseCase`() = runTest {
        // Given
        val appsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
        val apps = listOf(mockk<InstalledApp>())
        every { getManagedProfileAppsUseCase(ManagedProfileType.WorkApps) } returns appsFlow
        
        // Re-init to pick up the new flow
        viewModel = WorkAppsViewModel(
            getManagedProfileAppsUseCase,
            observeManagedProfileUnlockedUseCase,
            toggleManagedProfileUseCase,
            canToggleManagedProfileUseCase
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.workApps.collect {} }

        // When
        appsFlow.value = apps

        // Then
        assertEquals(apps, viewModel.workApps.value)
    }

    @Test
    fun `toggleWorkProfile calls toggleManagedProfileUseCase and handles failure`() = runTest {
        // Given
        coEvery { toggleManagedProfileUseCase(ManagedProfileType.WorkApps) } returns ToggleManagedProfileUseCaseOutput.FailedNotDefaultLauncher
        var onLauncherNotDefaultCalled = false

        // When
        viewModel.toggleWorkProfile(onLauncherNotDefault = { onLauncherNotDefaultCalled = true })
        advanceUntilIdle()

        // Then
        assertEquals(true, onLauncherNotDefaultCalled)
    }

    @Test
    fun `toggleWorkProfile calls toggleManagedProfileUseCase and handles success`() = runTest {
        // Given
        coEvery { toggleManagedProfileUseCase(ManagedProfileType.WorkApps) } returns ToggleManagedProfileUseCaseOutput.SuccessfulToggleOn
        var onLauncherNotDefaultCalled = false

        // When
        viewModel.toggleWorkProfile(onLauncherNotDefault = { onLauncherNotDefaultCalled = true })
        advanceUntilIdle()

        // Then
        assertEquals(false, onLauncherNotDefaultCalled)
    }
}
