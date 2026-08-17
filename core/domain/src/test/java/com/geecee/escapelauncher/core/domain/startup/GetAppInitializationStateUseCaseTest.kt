package com.geecee.escapelauncher.core.domain.startup

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.repository.settings.AppearanceRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.ModifiedApp
import io.mockk.every
import io.mockk.mockk
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

        useCase = GetAppInitializationStateUseCase(
            appsRepository,
            modifiedAppsRepository,
            appearanceRepository,
            screenTimeRepository,
            screenTimeSettingsRepository
        )
    }

    @Test
    fun `when all data is loaded, isAllLoaded returns true`() = runTest {
        // Given
        installedAppsFlow.value = listOf(mockk())

        // When
        val state = useCase().first()

        // Then
        assertTrue(state.isAppsLoaded)
        assertTrue(state.isFavoritesLoaded)
        assertTrue(state.isSettingsLoaded)
        assertTrue(state.isScreenTimeLoaded)
        assertTrue(state.isAllLoaded)
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
        assertFalse(state.isAllLoaded)
    }
}
