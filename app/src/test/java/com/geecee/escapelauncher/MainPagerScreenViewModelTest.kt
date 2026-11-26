package com.geecee.escapelauncher

import android.app.Application
import com.geecee.escapelauncher.core.domain.apps.LaunchAppUseCase
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppResult
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppUseCase
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainPagerScreenViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val settings = mockk<LauncherBehaviorRepository>(relaxed = true)
    private val tryOpen = mockk<TryOpenAppUseCase>()
    private val launchApp = mockk<LaunchAppUseCase>()
    private val app = mockk<InstalledApp> { every { packageName } returns "test.app" }
    private lateinit var viewModel: MainPagerScreenViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { settings.countdownSeconds } returns flowOf(3)
        coEvery { tryOpen(any(), any()) } returns TryOpenAppResult.ShowChallenge
        every { launchApp(any(), any()) } returns false
        viewModel = MainPagerScreenViewModel(
            context = mockk<Application>(),
            onboardingRepository = mockk(relaxed = true),
            screenTimeSettingsRepository = mockk<ScreenTimeSettingsRepository> {
                every { hideScreenTimePage } returns flowOf(false)
            },
            launcherBehaviorRepository = settings,
            widgetSettingsRepository = mockk(relaxed = true),
            appearanceRepository = mockk<AppearanceRepository>(relaxed = true) {
                every { homeAlignment } returns flowOf("Left")
            },
            widgetHostManager = mockk(relaxed = true),
            tryOpenAppUseCase = tryOpen,
            launchAppUseCase = launchApp,
            managedProfileExistsUseCase = mockk(relaxed = true),
            isManagedProfileSupportedUseCase = mockk(relaxed = true),
            getIsDefaultLauncherUseCase = mockk(relaxed = true),
            lockScreenUseCase = mockk(relaxed = true),
            modifiedAppsRepository = mockk(relaxed = true),
            getAppActionsUseCase = mockk(relaxed = true),
            getAppShortcutsUseCase = mockk(relaxed = true),
            startShortcutUseCase = mockk(relaxed = true),
            uninstallAppUseCase = mockk(relaxed = true),
            openAppDetailsUseCase = mockk(relaxed = true),
            navRouter = mockk(relaxed = true),
            appConfiguration = mockk(relaxed = true),
            weatherSettingsRepository = mockk(relaxed = true)
        )
    }

    @After
    fun tearDown() {
        viewModel.cancelCountdown()
        Dispatchers.resetMain()
    }

    @Test
    fun unchallengedAppLaunchesImmediatelyAndReportsSuccess() = runTest(dispatcher) {
        coEvery { tryOpen(any(), any()) } returns TryOpenAppResult.Launch
        val onOpened = mockk<(String) -> Unit>(relaxed = true)
        every { launchApp(app, onOpened) } answers {
            onOpened(app.packageName)
            true
        }

        viewModel.openApp(app, onAppOpened = onOpened)
        runCurrent()

        assertNull(viewModel.countdownRemaining.value)
        verify(exactly = 1) { launchApp(app, onOpened) }
        verify(exactly = 1) { onOpened("test.app") }
        verify(exactly = 0) { settings.countdownSeconds }

        // Exercise the successful launch's delayed pager reset as well.
        advanceTimeBy(500)
        runCurrent()
        assertEquals(1, viewModel.pagerState.currentPage)
        verify(exactly = 1) { launchApp(app, onOpened) }
    }

    @Test
    fun launchesExactlyOnceAfterFullDuration() = runTest(dispatcher) {
        viewModel.openApp(app)
        runCurrent()
        assertEquals(3, viewModel.countdownRemaining.value)
        advanceTimeBy(2999)
        verify(exactly = 0) { launchApp(any(), any()) }
        advanceTimeBy(1)
        runCurrent()
        assertNull(viewModel.countdownRemaining.value)
        advanceTimeBy(600_000)
        verify(exactly = 1) { launchApp(app, any()) }
    }

    @Test
    fun cancelAtLastTickPreventsLaunch() = runTest(dispatcher) {
        viewModel.openApp(app)
        runCurrent()
        advanceTimeBy(3000)
        viewModel.cancelCountdown()
        runCurrent()
        advanceTimeBy(600_000)
        verify(exactly = 0) { launchApp(any(), any()) }
        assertNull(viewModel.countdownRemaining.value)
    }

    @Test
    fun replacementGetsFullDurationAndOnlyLaunchesNewApp() = runTest(dispatcher) {
        val second = mockk<InstalledApp> { every { packageName } returns "second.app" }
        viewModel.openApp(app)
        runCurrent()
        advanceTimeBy(2000)
        viewModel.openApp(second)
        runCurrent()
        assertEquals(3, viewModel.countdownRemaining.value)
        advanceTimeBy(3000)
        runCurrent()
        verify(exactly = 0) { launchApp(app, any()) }
        verify(exactly = 1) { launchApp(second, any()) }
    }

    @Test
    fun waitsForPersistedDurationAndCanCancelWhileLoading() = runTest(dispatcher) {
        every { settings.countdownSeconds } returns flow { delay(1000); emit(600) }
        viewModel.openApp(app)
        runCurrent()
        assertNull(viewModel.countdownRemaining.value)
        advanceTimeBy(1000)
        runCurrent()
        assertEquals(600, viewModel.countdownRemaining.value)
        viewModel.cancelCountdown()
        viewModel.openApp(app)
        runCurrent()
        viewModel.cancelCountdown()
        advanceTimeBy(700_000)
        verify(exactly = 0) { launchApp(any(), any()) }
    }
}
