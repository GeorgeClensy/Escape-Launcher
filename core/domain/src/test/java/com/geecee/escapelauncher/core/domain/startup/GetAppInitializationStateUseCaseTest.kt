package com.geecee.escapelauncher.core.domain.startup

import com.geecee.escapelauncher.core.analytics.AnalyticsProxy
import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.ModifiedApp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAppInitializationStateUseCaseTest {

    private lateinit var appsRepository: AppsRepository
    private lateinit var modifiedAppsRepository: ModifiedAppsRepository
    private lateinit var appearanceRepository: AppearanceRepository
    private lateinit var screenTimeRepository: ScreenTimeRepository
    private lateinit var screenTimeSettingsRepository: ScreenTimeSettingsRepository
    private lateinit var appStartupMigrationUseCase: AppStartupMigrationUseCase
    private lateinit var analyticsProxy: AnalyticsProxy
    private lateinit var useCase: GetAppInitializationStateUseCase

    private val installedAppsFlow = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val favoritesFlow = flowOf(emptyList<ModifiedApp>())
    private val showStatusBarFlow = flowOf(true)
    private val allUsageFlow = flowOf(emptyList<com.geecee.escapelauncher.core.model.AppUsage>())
    private val hideScreenTimePageFlow = flowOf(false)

    @Before
    fun setUp() {
        appsRepository = mockk {
            every { installedApps } returns installedAppsFlow
        }
        modifiedAppsRepository = mockk {
            every { getFavouriteAppsInOrderFlow() } returns favoritesFlow
        }
        appearanceRepository = mockk {
            every { showStatusBar } returns showStatusBarFlow
        }
        screenTimeRepository = mockk {
            every { allUsageFlow } returns this@GetAppInitializationStateUseCaseTest.allUsageFlow
        }
        screenTimeSettingsRepository = mockk {
            every { hideScreenTimePage } returns hideScreenTimePageFlow
        }
        appStartupMigrationUseCase = mockk {
            coEvery { this@mockk.invoke() } just runs
        }
        analyticsProxy = mockk(relaxed = true)

        useCase = GetAppInitializationStateUseCase(
            appsRepository,
            modifiedAppsRepository,
            appearanceRepository,
            screenTimeRepository,
            screenTimeSettingsRepository,
            appStartupMigrationUseCase,
            analyticsProxy
        )
    }

    @Test
    fun `when all data is loaded and migration finishes, isAllLoaded returns true`() = runTest {
        // Given
        installedAppsFlow.value = listOf(mockk())

        // When
        val state = useCase().first()

        // Then
        assertTrue(state.isAppsLoaded)
        assertTrue(state.isFavoritesLoaded)
        assertTrue(state.isSettingsLoaded)
        assertTrue(state.isScreenTimeLoaded)
        assertTrue(state.isMigrationComplete)
        assertTrue(state.isAllLoaded)
        coVerify(exactly = 1) { appStartupMigrationUseCase() }
    }

    @Test
    fun `when apps are not loaded, isAllLoaded returns false`() = runTest {
        // Given
        installedAppsFlow.value = emptyList()

        // When
        val state = useCase().first()

        // Then
        assertFalse(state.isAppsLoaded)
        assertTrue(state.isFavoritesLoaded)
        assertTrue(state.isSettingsLoaded)
        assertTrue(state.isScreenTimeLoaded)
        assertTrue(state.isMigrationComplete)
        assertFalse(state.isAllLoaded)
    }

    @Test
    fun `when migration throws exception, it records exception to analytics and still emits true`() = runTest {
        // Given
        installedAppsFlow.value = listOf(mockk())
        val exception = RuntimeException("Migration failed")
        coEvery { appStartupMigrationUseCase() } throws exception

        // When
        val state = useCase().first()

        // Then
        assertTrue(state.isMigrationComplete)
        assertTrue(state.isAllLoaded)
        coVerify(exactly = 1) { analyticsProxy.recordException(exception) }
    }
}
