package com.geecee.escapelauncher.privatespace

import com.geecee.escapelauncher.core.domain.managedprofiles.CanToggleManagedProfileUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.GetManagedProfileAppsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.managedprofiles.ObserveManagedProfileUnlockedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ToggleManagedProfileUseCaseOutput
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import io.mockk.coEvery
import io.mockk.coVerify
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
class PrivateSpaceViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getManagedProfileAppsUseCase: GetManagedProfileAppsUseCase = mockk()
    private val observeManagedProfileUnlockedUseCase: ObserveManagedProfileUnlockedUseCase = mockk()
    private val toggleManagedProfileUseCase: ToggleManagedProfileUseCase = mockk()
    private val canToggleManagedProfileUseCase: CanToggleManagedProfileUseCase = mockk()
    private val launcherBehaviorRepository: LauncherBehaviorRepository = mockk()

    private lateinit var viewModel: PrivateSpaceViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Default mocks for initialization
        every { canToggleManagedProfileUseCase(ManagedProfileType.PrivateSpace) } returns true
        every { observeManagedProfileUnlockedUseCase(ManagedProfileType.PrivateSpace) } returns flowOf(true)
        every { getManagedProfileAppsUseCase(ManagedProfileType.PrivateSpace) } returns flowOf(emptyList())
        every { launcherBehaviorRepository.hidePrivateSpace } returns flowOf(false)

        viewModel = PrivateSpaceViewModel(
            getManagedProfileAppsUseCase,
            observeManagedProfileUnlockedUseCase,
            toggleManagedProfileUseCase,
            canToggleManagedProfileUseCase,
            launcherBehaviorRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `canToggleProfile reflects use case result`() {
        assertEquals(true, viewModel.canToggleProfile)
        verify { canToggleManagedProfileUseCase(ManagedProfileType.PrivateSpace) }
    }

    @Test
    fun `isUnlocked reflects observeManagedProfileUnlockedUseCase`() = runTest {
        // Given
        val unlockedFlow = MutableStateFlow(false)
        every { observeManagedProfileUnlockedUseCase(ManagedProfileType.PrivateSpace) } returns unlockedFlow

        // Re-init to pick up the new flow
        viewModel = PrivateSpaceViewModel(
            getManagedProfileAppsUseCase,
            observeManagedProfileUnlockedUseCase,
            toggleManagedProfileUseCase,
            canToggleManagedProfileUseCase,
            launcherBehaviorRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.isUnlocked.collect {} }

        // When
        unlockedFlow.value = true

        // Then
        assertEquals(true, viewModel.isUnlocked.value)
    }

    @Test
    fun `privateSpaceApps reflects getManagedProfileAppsUseCase`() = runTest {
        // Given
        val appsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
        val apps = listOf(mockk<InstalledApp>())
        every { getManagedProfileAppsUseCase(ManagedProfileType.PrivateSpace) } returns appsFlow

        // Re-init to pick up the new flow
        viewModel = PrivateSpaceViewModel(
            getManagedProfileAppsUseCase,
            observeManagedProfileUnlockedUseCase,
            toggleManagedProfileUseCase,
            canToggleManagedProfileUseCase,
            launcherBehaviorRepository
        )

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.privateSpaceApps.collect {} }

        // When
        appsFlow.value = apps

        // Then
        assertEquals(apps, viewModel.privateSpaceApps.value)
    }

    @Test
    fun `toggleSettings changes showSettings state`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.showSettings.collect {} }
        
        assertEquals(false, viewModel.showSettings.value)
        viewModel.toggleSettings()
        assertEquals(true, viewModel.showSettings.value)
        viewModel.toggleSettings()
        assertEquals(false, viewModel.showSettings.value)
    }

    @Test
    fun `togglePrivateSpaceProfile calls toggleManagedProfileUseCase and handles failure`() = runTest {
        // Given
        coEvery { toggleManagedProfileUseCase(ManagedProfileType.PrivateSpace) } returns ToggleManagedProfileUseCaseOutput.FailedNotDefaultLauncher
        var onLauncherNotDefaultCalled = false

        // When
        viewModel.togglePrivateSpaceProfile(onLauncherNotDefault = { onLauncherNotDefaultCalled = true })
        advanceUntilIdle()

        // Then
        assertEquals(true, onLauncherNotDefaultCalled)
    }

    @Test
    fun `setHiddenPrivateSpace calls repository`() = runTest {
        // Given
        coEvery { launcherBehaviorRepository.setHidePrivateSpace(any()) } returns Unit

        // When
        viewModel.setHiddenPrivateSpace(true)
        advanceUntilIdle()

        // Then
        coVerify { launcherBehaviorRepository.setHidePrivateSpace(true) }
    }
}
