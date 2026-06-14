package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.view.Window
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.launchApp
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Home Screen View Model - Used for holding UI state for the home screen pages
 */
class HomeScreenModel(application: Application, val mainAppViewModel: MainAppViewModel) :
    AndroidViewModel(application) {
    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))


    var showOpenChallenge = mutableStateOf(false)

    val coroutineScope = viewModelScope
    val interactionSource = MutableInteractionSource()

    val favoriteApps = mutableStateListOf<InstalledApp>()

    val appsListScrollState = LazyListState()

    val hideScreenTimePage = mainAppViewModel.settingsRepository.hideScreenTimePage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

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

        if (pagerState.isScrollInProgress && pagerState.targetPage == targetPage) {
            return
        }

        pagerState.animateScrollToPage(
            targetPage,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
    }

    init {
        coroutineScope.launch {
            combine(
                mainAppViewModel.appsRepository.mainUserApps,
                mainAppViewModel.modifiedAppsRepository.getFavouriteAppsInOrderFlow()
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
        mainAppViewModel.isAppOpened = true
        mainAppViewModel.shouldGoHomeOnResume.value = true
        updateSelectedApp(app)
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
                mainAppViewModel.modifiedAppsRepository.isChallenge(app.packageName)
            }

            if (hasChallenge) {
                showOpenChallenge.value = true
                updateSelectedApp(app)
            } else {
                if (launchApp(getApplication(), app, onAppOpened)) {
                    onAppLaunched(app)
                    if (overrideChallenge) {
                        showOpenChallenge.value = false
                    }
                }
            }
        }
    }
}

class HomeScreenModelFactory(
    private val application: Application,
    private val mainAppViewModel: MainAppViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeScreenModel(application, mainAppViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * Main App View Model - Used for data that needs to be passed around the app
 */
@HiltViewModel
class MainAppViewModel @Inject constructor(
    application: Application,
    val modifiedAppsRepository: ModifiedAppsRepository,
    val appsRepository: AppsRepository,
    val settingsRepository: SettingsRepository
) : AndroidViewModel(application) {
    private var window: Window? = null

    fun setWindow(window: Window) {
        this.window = window
    }

    fun getWindow(): Window? = window

    // Loading states for splash screen
    val isAppsLoaded = mutableStateOf(false)
    val isFavoritesLoaded = mutableStateOf(false)
    val isScreenTimeLoaded = mutableStateOf(false)

    init {
        // Keep isAppsLoaded in sync with repository
        viewModelScope.launch {
            appsRepository.installedApps.collect {
                if (it.isNotEmpty()) {
                    isAppsLoaded.value = true
                }
            }
        }

        // Keep isFavoritesLoaded in sync
        viewModelScope.launch {
            modifiedAppsRepository.getFavouriteAppsInOrderFlow().collect {
                isFavoritesLoaded.value = true
            }
        }
    }

    // Other stuff

    var isAppOpened: Boolean =
        false // Set to true when an app is opened and false when it is closed again, used mainly for screen time

    val shouldGoHomeOnResume: MutableState<Boolean> =
        mutableStateOf(false) // This is to check whether to go back to the first page of the home screen the next time onResume is called, It is only ever used once in AllApps when you come back from signing in to private space
}
