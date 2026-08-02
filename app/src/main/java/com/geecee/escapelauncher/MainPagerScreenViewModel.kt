package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.launchApp
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import com.geecee.escapelauncher.core.domain.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MainPagerScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SettingsRepository,
    private val appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) : AndroidViewModel(context as Application) {
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            repository.setFirstTimeHelp(value)
        }
    }

    val hideScreenTimePage = repository.hideScreenTimePage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
    val doubleTapToLock = repository.doubleTapToLock
    val hapticFeedBackEnabled = repository.hapticFeedBackEnabled

    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))

    var showOpenChallenge = mutableStateOf(false)

    val interactionSource = MutableInteractionSource()

    val favoriteApps = mutableStateListOf<InstalledApp>()

    val appsListScrollState = LazyListState()

    val pagerState = PagerState(
        currentPage = if (hideScreenTimePage.value) 0 else 1,
        currentPageOffsetFraction = 0f
    ) {
        if (hideScreenTimePage.value) 2 else 3
    }

    private fun getMainPageIndex(): Int {
        return if (hideScreenTimePage.value) 0 else 1
    }

    suspend fun goToMainPage() {
        pagerState.scrollToPage(getMainPageIndex())
    }

    suspend fun animatedGoToMainPage() {
        val targetPage = getMainPageIndex()

        if (pagerState.currentPage == targetPage && pagerState.currentPageOffsetFraction == 0f) {
            return
        }

        pagerState.animateScrollToPage(
            targetPage,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    init {
        viewModelScope.launch {
            combine(
                appsRepository.mainUserApps,
                modifiedAppsRepository.getFavouriteAppsInOrderFlow()
            ) { apps, entities ->
                entities.mapNotNull { entity ->
                    apps.find { it.packageName == entity.packageId }
                }
            }.collect { newFavoriteApps ->
                withContext(Dispatchers.Main) {
                    favoriteApps.clear()
                    favoriteApps.addAll(newFavoriteApps)
                }
            }
        }
    }

    fun updateSelectedApp(app: InstalledApp) {
        currentSelectedApp.value = app
    }

    /**
     * Logic to handle what happens when an app is launched
     */
    fun onAppLaunched(app: InstalledApp) {
        updateSelectedApp(app)
        viewModelScope.launch {
            delay(500.milliseconds)
            goToMainPage()
        }
    }

    /**
     * High-level function to open an app, handling challenge checks asynchronously
     */
    fun openApp(
        app: InstalledApp,
        overrideChallenge: Boolean = false,
        onAppOpened: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val hasChallenge = if (overrideChallenge) {
                false
            } else {
                modifiedAppsRepository.isChallenge(app.packageName)
            }

            if (hasChallenge) {
                showOpenChallenge.value = true
                updateSelectedApp(app)
            } else {
                if (launchApp(getApplication(), app, onAppOpened)) {
                    onAppLaunched(app)

                    // At the end of an open challenge countdown, it runs this openApp function again with overrideChallenge set to true so this is used to hide the challenge ui
                    if (overrideChallenge) {
                        showOpenChallenge.value = false
                    }
                }
            }
        }
    }
}
