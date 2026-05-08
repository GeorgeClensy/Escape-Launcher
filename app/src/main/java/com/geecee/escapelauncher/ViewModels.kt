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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.getBooleanSetting
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Home Screen View Model - Used for holding UI state for the home screen pages
 */
class HomeScreenModel(application: Application, val mainAppViewModel: MainAppViewModel) :
    AndroidViewModel(application) {
    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))

    val isCurrentAppFavorite by derivedStateOf {
        favoriteApps.contains(currentSelectedApp.value)
    }

    var showOpenChallenge = mutableStateOf(false)
    var showBottomSheet = mutableStateOf(false)

    val coroutineScope = viewModelScope
    val interactionSource = MutableInteractionSource()

    val favoriteApps = mutableStateListOf<InstalledApp>()

    val appsListScrollState = LazyListState()

    val pagerState = PagerState(
        currentPage = if (getBooleanSetting(
                context = mainAppViewModel.getContext(),
                setting = mainAppViewModel.getContext().resources.getString(R.string.hideScreenTimePage),
                defaultValue = false
            )
        ) {
            0
        } else {
            1
        },
        currentPageOffsetFraction = 0f
    ) {
        if (getBooleanSetting(
                context = mainAppViewModel.getContext(),
                setting = mainAppViewModel.getContext().resources.getString(R.string.hideScreenTimePage),
                defaultValue = false
            )
        ) {
            2
        } else {
            3
        }
    }

    private fun getMainPageIndex(): Int {
        val hideScreenTime = getBooleanSetting(
            context = mainAppViewModel.getContext(),
            setting = mainAppViewModel.getContext().resources.getString(R.string.hideScreenTimePage),
            defaultValue = false
        )
        return if (hideScreenTime) 0 else 1
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

    var showWorkBottomSheet = mutableStateOf(false)

    var showWorkApps = mutableStateOf(false)

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
                    mainAppViewModel.isFavoritesLoaded.value = true
                }
            }
        }

        // Keep isAppsLoaded in sync with repository
        coroutineScope.launch {
            mainAppViewModel.appsRepository.installedApps.collect {
                if (it.isNotEmpty()) {
                    mainAppViewModel.isAppsLoaded.value = true
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
                if (AppUtils.launchApp(getApplication(), app, onAppOpened)) {
                    onAppLaunched(app)
                    if (overrideChallenge) {
                        showOpenChallenge.value = false
                    }
                }
                AppUtils.resetHome(this@HomeScreenModel)
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
    val appsRepository: AppsRepository
) : AndroidViewModel(application) {
    private val appContext: android.content.Context = application.applicationContext // The app context

    private val _navigateHomeEvent = MutableSharedFlow<Unit>(
        replay = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
        extraBufferCapacity = 1
    )
    val navigateHomeEvent = _navigateHomeEvent.asSharedFlow()

    fun requestToGoHome() {
        viewModelScope.launch {
            _navigateHomeEvent.emit(Unit)
        }
    }

    fun getContext(): android.content.Context = appContext // Returns the context

    private var window: Window? = null

    fun setWindow(window: Window) {
        this.window = window
    }

    fun getWindow(): Window? = window

    // Loading states for splash screen
    val isAppsLoaded = mutableStateOf(false)
    val isFavoritesLoaded = mutableStateOf(false)
    val isScreenTimeLoaded = mutableStateOf(false)

    // Other stuff

    var isAppOpened: Boolean =
        false // Set to true when an app is opened and false when it is closed again, used mainly for screen time

    val shouldGoHomeOnResume: MutableState<Boolean> =
        mutableStateOf(false) // This is to check whether to go back to the first page of the home screen the next time onResume is called, It is only ever used once in AllApps when you come back from signing in to private space
}
