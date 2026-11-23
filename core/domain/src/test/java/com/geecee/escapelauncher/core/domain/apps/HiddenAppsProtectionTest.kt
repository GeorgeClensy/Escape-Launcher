package com.geecee.escapelauncher.core.domain.apps

import com.geecee.escapelauncher.core.domain.repository.android.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.db.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.HiddenAppsPasswordRepository
import com.geecee.escapelauncher.core.domain.search.SearchAppsUseCase
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.model.ModifiedApp
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class HiddenAppsProtectionTest {
    private val apps = mockk<AppsRepository>()
    private val modified = mockk<ModifiedAppsRepository>()
    private val password = mockk<HiddenAppsPasswordRepository>()
    private val protected = MutableStateFlow(true)
    private val hiddenApp = mockk<InstalledApp> {
        every { packageName } returns "hidden.app"
        every { displayName } returns "Hidden app"
    }

    init {
        every { password.isPasswordSet } returns protected
        every { apps.mainUserApps } returns MutableStateFlow(listOf(hiddenApp))
        every { modified.getHiddenPackageIdsFlow() } returns flowOf(listOf("hidden.app"))
        every { modified.getFavouriteAppsInOrderFlow() } returns flowOf(listOf(
            ModifiedApp("hidden.app", null, true, false, 0.0)
        ))
        coEvery { modified.isHidden("hidden.app") } returns true
    }

    @Test fun protectedAppsStayOutOfSearchEvenWhenShowHiddenIsEnabled() = runTest {
        val useCase = SearchAppsUseCase(apps, modified, password)
        assertTrue(useCase(flowOf("Hidden"), flowOf(true)).first().isEmpty())
        protected.value = false
        assertEquals(listOf(hiddenApp), useCase(flowOf("Hidden"), flowOf(true)).first())
    }

    @Test fun protectedHiddenFavouritesAreExcludedWithoutRemovingTheirSavedOrder() = runTest {
        val useCase = GetFavoriteAppsUseCase(apps, modified, password)
        assertTrue(useCase().first().isEmpty())
        protected.value = false
        assertEquals(listOf(hiddenApp), useCase().first())
    }

    @Test fun challengeOverrideCannotBypassHiddenAppProtection() = runTest {
        assertEquals(TryOpenAppResult.RequireHiddenAppsUnlock,
            TryOpenAppUseCase(modified, password)("hidden.app", bypassChallenge = true))
        protected.value = false
        assertEquals(TryOpenAppResult.Launch,
            TryOpenAppUseCase(modified, password)("hidden.app", bypassChallenge = true))
    }
}
