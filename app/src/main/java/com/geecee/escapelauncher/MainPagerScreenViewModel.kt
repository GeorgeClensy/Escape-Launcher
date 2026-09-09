package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.apps.LaunchAppUseCase
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppResult
import com.geecee.escapelauncher.core.domain.apps.TryOpenAppUseCase
import com.geecee.escapelauncher.core.domain.launcher.GetIsDefaultLauncherUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.IsManagedProfileSupportedUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileExistsUseCase
import com.geecee.escapelauncher.core.domain.managedprofiles.ManagedProfileType
import com.geecee.escapelauncher.core.domain.repository.settings.OnboardingRepository
import com.geecee.escapelauncher.core.domain.repository.settings.ScreenTimeSettingsRepository
import com.geecee.escapelauncher.core.domain.repository.settings.LauncherBehaviorRepository
import com.geecee.escapelauncher.core.domain.repository.settings.TodoSettingsRepository
import com.geecee.escapelauncher.core.domain.system.LockScreenUseCase
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/** The pages of the home pager. */
enum class PagerPage { SCREEN_TIME, TODO, HOME, APPS }

@HiltViewModel
class MainPagerScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val onboardingRepository: OnboardingRepository,
    screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    todoSettingsRepository: TodoSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    private val tryOpenAppUseCase: TryOpenAppUseCase,
    private val launchAppUseCase: LaunchAppUseCase,
    private val managedProfileExistsUseCase: ManagedProfileExistsUseCase,
    private val isManagedProfileSupportedUseCase: IsManagedProfileSupportedUseCase,
    private val getIsDefaultLauncherUseCase: GetIsDefaultLauncherUseCase,
    private val lockScreenUseCase: LockScreenUseCase
) : AndroidViewModel(context as Application) {
    fun managedProfileExists(type: ManagedProfileType): Boolean = managedProfileExistsUseCase(type)
    fun isManagedProfileSupported(type: ManagedProfileType): Boolean = isManagedProfileSupportedUseCase(type)
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            onboardingRepository.setFirstTimeHelp(value)
        }
    }

    /**
     * The pages of the home pager, left to right. Which optional pages appear is driven by
     * settings, so everything that needs an index looks it up here instead of hard-coding it.
     */
    val pages: StateFlow<List<PagerPage>> = combine(
        screenTimeSettingsRepository.hideScreenTimePage,
        todoSettingsRepository.showTodoPage
    ) { hideScreenTime, showTodo ->
        buildList {
            if (!hideScreenTime) add(PagerPage.SCREEN_TIME)
            if (showTodo) add(PagerPage.TODO)
            add(PagerPage.HOME)
            add(PagerPage.APPS)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = listOf(PagerPage.SCREEN_TIME, PagerPage.TODO, PagerPage.HOME, PagerPage.APPS)
    )
    val doubleTapToLock = launcherBehaviorRepository.doubleTapToLock
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled

    val isHiddenPrivateSpace = launcherBehaviorRepository.hidePrivateSpace

    private val _isDefaultLauncher = MutableStateFlow(false)
    val isDefaultLauncher: StateFlow<Boolean> = _isDefaultLauncher.asStateFlow()

    fun updateLauncherStatus() {
        _isDefaultLauncher.value = getIsDefaultLauncherUseCase()
    }

    fun lockScreen() {
        lockScreenUseCase()
    }

    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))

    var showOpenChallenge = mutableStateOf(false)

    val interactionSource = MutableInteractionSource()

    val appsListScrollState = LazyListState()

    val pagerState = PagerState(
        currentPage = pages.value.indexOf(PagerPage.HOME),
        currentPageOffsetFraction = 0f
    ) {
        pages.value.size
    }

    private fun getMainPageIndex(): Int = pages.value.indexOf(PagerPage.HOME)

    fun indexOf(page: PagerPage): Int = pages.value.indexOf(page)

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
        updateLauncherStatus()

        // The pager is constructed before DataStore has emitted, so `pages` is still its
        // placeholder value at that point. Re-anchor the pager on the main page once the real
        // value is known, and again whenever a page is toggled (the page indices shift).
        viewModelScope.launch {
            pages.collect { pagerState.scrollToPage(it.indexOf(PagerPage.HOME)) }
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
            when (tryOpenAppUseCase(app.packageName, overrideChallenge)) {
                TryOpenAppResult.ShowChallenge -> {
                    showOpenChallenge.value = true
                    updateSelectedApp(app)
                }
                TryOpenAppResult.Launch -> {
                    if (launchAppUseCase(app, onAppOpened)) {
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
}
