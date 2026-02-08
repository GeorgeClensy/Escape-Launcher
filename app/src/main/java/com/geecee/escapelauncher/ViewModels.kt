package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.util.Log
import android.view.Window
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.ui.theme.AppTheme
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.InstalledApp
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.managers.ChallengesManager
import com.geecee.escapelauncher.utils.managers.FavoriteAppsManager
import com.geecee.escapelauncher.utils.managers.HiddenAppsManager
import com.geecee.escapelauncher.utils.managers.getScreenTimeListSorted
import com.geecee.escapelauncher.utils.managers.getUsageForApp
import com.geecee.escapelauncher.utils.weatherProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home Screen View Model - Used for holding UI state for the home screen pages
 */
class HomeScreenModel(application: Application, val mainAppViewModel: MainAppViewModel) :
    AndroidViewModel(application) {
    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))
    val isCurrentAppChallenged by derivedStateOf {
        mainAppViewModel.challengesTrigger.intValue
        mainAppViewModel.challengesManager.doesAppHaveChallenge(currentSelectedApp.value.packageName)
    }
    val isCurrentAppFavorite by derivedStateOf {
        favoriteApps.contains(currentSelectedApp.value)
    }

    var showOpenChallenge = mutableStateOf(false)
    var showBottomSheet = mutableStateOf(false)
    var showPrivateSpaceSettings = mutableStateOf(false)

    var searchText = mutableStateOf("")
    var searchExpanded = mutableStateOf(false)

    val coroutineScope = viewModelScope
    val interactionSource = MutableInteractionSource()

    val installedApps = mutableStateListOf<InstalledApp>()

    val filteredApps = mutableStateListOf<InstalledApp>()

    fun updateFilteredApps() {
        // Take a snapshot of the list and other state on the main thread to avoid ConcurrentModificationException
        // when iterating on Dispatchers.Default while the original list is being modified.
        val appsSnapshot = installedApps.toList()
        val query = searchText.value.trim()
        val context = mainAppViewModel.getContext()
        val showHiddenInSearch = getBooleanSetting(
            context,
            context.resources.getString(R.string.showHiddenAppsInSearch),
            false
        )

        coroutineScope.launch(Dispatchers.Default) {
            val apps = appsSnapshot.filter {
                it.packageName != context.packageName
            }

            val filtered = if (query.isBlank()) {
                apps.filter { !mainAppViewModel.hiddenAppsManager.isAppHidden(it.packageName) }
            } else {
                val regexUnaccentPattern = Regex("\\p{M}+")
                apps.filter { app ->
                    val isHidden =
                        mainAppViewModel.hiddenAppsManager.isAppHidden(app.packageName)
                    val matchesQuery = AppUtils.fuzzyMatch(app.displayName, query)
                    matchesQuery && (!isHidden || showHiddenInSearch)
                }.sortedWith(compareBy<InstalledApp> { app ->
                    val normalizedQuery = Normalizer.normalize(query, Normalizer.Form.NFD)
                        .replace(regexUnaccentPattern, "")
                        .lowercase()

                    val normalizedName = Normalizer.normalize(app.displayName, Normalizer.Form.NFD)
                        .replace(regexUnaccentPattern, "")
                        .lowercase()

                    when {
                        normalizedName.startsWith(normalizedQuery) -> 0
                        normalizedName.contains(normalizedQuery) -> 1
                        else -> 2
                    }
                }.thenBy { it.displayName.lowercase() })
            }
            withContext(Dispatchers.Main) {
                filteredApps.clear()
                filteredApps.addAll(filtered)
            }
        }
    }

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

    suspend fun goToMainPage() {
        if (getBooleanSetting(
                context = mainAppViewModel.getContext(),
                setting = mainAppViewModel.getContext().resources.getString(R.string.hideScreenTimePage),
                defaultValue = false
            )
        ) {
            pagerState.scrollToPage(0)
        } else {
            pagerState.scrollToPage(1)
        }
    }

    suspend fun animatedGoToMainPage() {
        if (getBooleanSetting(
                context = mainAppViewModel.getContext(),
                setting = mainAppViewModel.getContext().resources.getString(R.string.hideScreenTimePage),
                defaultValue = false
            )
        ) {
            pagerState.animateScrollToPage(
                0,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        } else {
            pagerState.animateScrollToPage(
                1,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
        }
    }

    val currentSelectedPrivateApp =
        mutableStateOf(InstalledApp("", "", ComponentName("", ""))) //Only used for the bottom sheet

    val currentSelectedWorkApp =
        mutableStateOf(InstalledApp("", "", ComponentName("", ""))) //Only used for the bottom sheet

    var showPrivateBottomSheet = mutableStateOf(false)

    var showWorkBottomSheet = mutableStateOf(false)

    var showWorkApps = mutableStateOf(false)

    init {
        loadApps()
        coroutineScope.launch {
            androidx.compose.runtime.snapshotFlow {
                Triple(searchText.value, mainAppViewModel.hiddenAppsTrigger.intValue, Unit)
            }.collect {
                updateFilteredApps()
            }
        }
    }

    fun loadApps() {
        Log.d("Loading", "LoadApps started")
        coroutineScope.launch {
            suspendLoadApps()
            suspendReloadFavouriteApps()
        }
    }

    private suspend fun suspendLoadApps() {
        Log.d("Loading", "SuspendLoadApps started")
        val apps = withContext(Dispatchers.IO) {
            AppUtils.getAllInstalledApps(mainAppViewModel.getContext()).sortedBy {
                it.displayName.lowercase()
            }
        }
        withContext(Dispatchers.Main) {
            installedApps.clear()
            installedApps.addAll(apps)
            updateFilteredApps()
            mainAppViewModel.isAppsLoaded.value = true
        }
    }

    fun reloadFavouriteApps() {
        coroutineScope.launch {
            suspendReloadFavouriteApps()
        }
    }

    private suspend fun suspendReloadFavouriteApps() {
        Log.d("Loading", "SuspendReloadFavouriteApps started")

        val favoritePackageNames = withContext(Dispatchers.IO) {
            mainAppViewModel.favoriteAppsManager.getFavoriteApps()
        }

        withContext(Dispatchers.Main) {
            val newFavoriteApps = favoritePackageNames.mapNotNull { packageName ->
                installedApps.find { it.packageName == packageName }
            }
            favoriteApps.clear()
            favoriteApps.addAll(newFavoriteApps)
            mainAppViewModel.isFavoritesLoaded.value = true
        }
    }

    fun updateSelectedApp(app: InstalledApp) {
        currentSelectedApp.value = app
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
class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = application.applicationContext // The app context

    private val _navigateHomeEvent = MutableSharedFlow<Unit>(replay = 0)
    val navigateHomeEvent = _navigateHomeEvent.asSharedFlow()

    fun requestToGoHome() {
        viewModelScope.launch {
            _navigateHomeEvent.emit(Unit)
        }
    }

    fun getContext(): Context = appContext // Returns the context

    private var window: Window? = null

    fun setWindow(window: Window) {
        this.window = window
    }

    fun getWindow(): Window? = window

    var appTheme: MutableState<AppTheme> = mutableStateOf(AppTheme.OFF_LIGHT) // App material theme

    // Loading states for splash screen
    val isAppsLoaded = mutableStateOf(false)
    val isFavoritesLoaded = mutableStateOf(false)
    val isThemeLoaded = mutableStateOf(false)
    val isScreenTimeLoaded = mutableStateOf(false)

    val isReady by derivedStateOf {
        isAppsLoaded.value && isFavoritesLoaded.value && isThemeLoaded.value && isScreenTimeLoaded.value
    }

    // Managers

    val favoriteAppsManager: FavoriteAppsManager =
        FavoriteAppsManager(application) // Favorite apps manager

    // Hidden Apps

    val hiddenAppsManager: HiddenAppsManager = HiddenAppsManager(application) // Hidden apps manager

    val hiddenAppsTrigger = mutableIntStateOf(0)

    fun notifyHiddenAppsChanged() {
        hiddenAppsTrigger.intValue++
    }

    // Open Countdown

    val challengesManager: ChallengesManager =
        ChallengesManager(application) // Manager for challenges

    val challengesTrigger = mutableIntStateOf(0)

    fun notifyChallengesChanged() {
        challengesTrigger.intValue++
    }

    // Other stuff

    var isAppOpened: Boolean =
        false // Set to true when an app is opened and false when it is closed again, used mainly for screen time

    val isPrivateSpaceUnlocked: MutableState<Boolean> =
        mutableStateOf(false) // If the private space is unlocked, set by a registered receiver when the private space is closed or opened

    val shouldGoHomeOnResume: MutableState<Boolean> =
        mutableStateOf(false) // This is to check whether to go back to the first page of the home screen the next time onResume is called, It is only ever used once in AllApps when you come back from signing into private space

    // Screen time related things

    private val dateFormat =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Format for the date

    fun getToday(): String {
        return dateFormat.format(Date())
    } // Returns the current date

    val screenTimeCache =
        mutableStateMapOf<String, Long>() // Cache mapping package name to screen time

    val shouldReloadScreenTime: MutableState<Int> =
        mutableIntStateOf(0) // This exists because the screen time is retrieved in LaunchedEffects so it'll reload when the value of this is changed

    fun updateAppScreenTime(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val screenTime = getUsageForApp(packageName, getToday())
            screenTimeCache[packageName] = screenTime
        }
    } // Function to update a single app's cached screen time

    fun reloadScreenTimeCache() {
        Log.d("Loading", "ReloadScreenTimeCache started")

        viewModelScope.launch(Dispatchers.IO) {
            val usageList = getScreenTimeListSorted(getToday())
            val usageMap = usageList.associate { it.packageName to it.totalTime }

            withContext(Dispatchers.Main) {
                screenTimeCache.clear()
                screenTimeCache.putAll(usageMap)
                shouldReloadScreenTime.value++
                isScreenTimeLoaded.value = true
            }
        }
    } // Reloads the screen times efficiently

    suspend fun getScreenTimeAsync(packageName: String, forceRefresh: Boolean = false): Long {
        if (forceRefresh || !screenTimeCache.containsKey(packageName)) {
            val screenTime = getUsageForApp(packageName, getToday())
            screenTimeCache[packageName] = screenTime
            return screenTime
        }
        return screenTimeCache[packageName] ?: 0L
    } // Function to get screen time from cache or compute if missing

    fun getCachedScreenTime(packageName: String): Long {
        return screenTimeCache[packageName] ?: 0L
    } // Non-suspend function that just returns the cached value without fetching

    // Weather
    val weatherText = mutableStateOf("")

    private var lastWeatherUpdate = 0L

    fun updateWeather() {
        val currentTime = System.currentTimeMillis()
        val useFahrenheit = getBooleanSetting(appContext, appContext.getString(R.string.UseFahrenheit))
        // Update weather if it's been more than 30 minutes or if it's empty
        if (currentTime - lastWeatherUpdate > 30 * 60 * 1000 || weatherText.value.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                weatherProxy.getWeather(appContext, useFahrenheit) { result ->
                    viewModelScope.launch(Dispatchers.Main) {
                        weatherText.value = result
                        // Only update the last update time if we got a valid-looking result
                        if (!result.contains("error", ignoreCase = true) &&
                            !result.contains("unavailable", ignoreCase = true)
                        ) {
                            lastWeatherUpdate = System.currentTimeMillis()
                        }
                    }
                }
            }
        }
    }

    fun forceUpdateWeather() {
        val useFahrenheit = getBooleanSetting(appContext, appContext.getString(R.string.UseFahrenheit))
        viewModelScope.launch(Dispatchers.IO) {
            weatherProxy.getWeather(appContext, useFahrenheit) { result ->
                viewModelScope.launch(Dispatchers.Main) {
                    weatherText.value = result
                    // Only update the last update time if we got a valid-looking result
                    if (!result.contains("error", ignoreCase = true) &&
                        !result.contains("unavailable", ignoreCase = true)
                    ) {
                        lastWeatherUpdate = System.currentTimeMillis()
                    }
                }
            }
        }
    }
}
