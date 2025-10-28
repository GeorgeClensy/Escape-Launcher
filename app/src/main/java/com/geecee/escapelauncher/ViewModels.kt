package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.ui.theme.offLightScheme
import com.geecee.escapelauncher.utils.AppUtils
import com.geecee.escapelauncher.utils.InstalledApp
import com.geecee.escapelauncher.utils.getBooleanSetting
import com.geecee.escapelauncher.utils.managers.ChallengesManager
import com.geecee.escapelauncher.utils.managers.FavoriteAppsManager
import com.geecee.escapelauncher.utils.managers.HiddenAppsManager
import com.geecee.escapelauncher.utils.managers.getUsageForApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Home Screen View Model
 */
class HomeScreenModel(application: Application, private val mainAppViewModel: MainAppViewModel) :
    AndroidViewModel(application) {
    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", "")))

    @Suppress("MemberVisibilityCanBePrivate")
    var isCurrentAppHidden = mutableStateOf(false)
    var isCurrentAppChallenged = mutableStateOf(false)
    var isCurrentAppFavorite = mutableStateOf(false)

    var showOpenChallenge = mutableStateOf(false)
    var showBottomSheet = mutableStateOf(false)
    var showPrivateSpaceSettings = mutableStateOf(false)

    var searchText = mutableStateOf("")
    var searchExpanded = mutableStateOf(false)

    val coroutineScope = viewModelScope
    val interactionSource = MutableInteractionSource()

    val installedApps = mutableStateListOf<InstalledApp>()
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

    val currentSelectedPrivateApp =
        mutableStateOf(InstalledApp("", "", ComponentName("", ""))) //Only used for the bottom sheet
    var showPrivateBottomSheet = mutableStateOf(false)

    init {
        loadApps()
        reloadFavouriteApps()
    }

    fun loadApps() {
        coroutineScope.launch {
            installedApps.clear()
            installedApps.addAll(
                AppUtils.getAllInstalledApps(mainAppViewModel.getContext()).sortedBy {
                    it.displayName
                })
        }
    }

    fun reloadFavouriteApps() {
        coroutineScope.launch {
            val newFavoriteApps = mainAppViewModel.favoriteAppsManager.getFavoriteApps()
                .mapNotNull { packageName ->
                    installedApps.find { it.packageName == packageName }
                }

            favoriteApps.apply {
                clear()
                addAll(newFavoriteApps)
            }
        }
    }

    fun updateSelectedApp(app: InstalledApp) {
        currentSelectedApp.value = app
        isCurrentAppFavorite.value = favoriteApps.contains(app)
        isCurrentAppChallenged.value =
            mainAppViewModel.challengesManager.doesAppHaveChallenge(app.packageName)
        isCurrentAppHidden.value = mainAppViewModel.hiddenAppsManager.isAppHidden(app.packageName)

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

class MainAppViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext: Context = application.applicationContext // The app context

    fun getContext(): Context = appContext // Returns the context

    var appTheme: MutableState<ColorScheme> = mutableStateOf(offLightScheme) // App material theme

    // Managers

    val favoriteAppsManager: FavoriteAppsManager =
        FavoriteAppsManager(application) // Favorite apps manager

    val hiddenAppsManager: HiddenAppsManager = HiddenAppsManager(application) // Hidden apps manager

    val challengesManager: ChallengesManager =
        ChallengesManager(application) // Manager for challenges

    // Other stuff

    var isAppOpened: Boolean =
        false // Set to true when an app is opened and false when it is closed again, used mainly for screen time

    val isPrivateSpaceUnlocked: MutableState<Boolean> =
        mutableStateOf(false) // If the private space is unlocked, set by a registered receiver when the private space is closed or opened

    val shouldGoHomeOnResume: MutableState<Boolean> =
        mutableStateOf(false) // This is to check whether to go back to the first page of the home screen the next time onResume is called, It is only ever used once in AllApps when you come back from signing into private space

    var blackOverlay = mutableStateOf(false)

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

    @Suppress("unused")
    fun reloadScreenTimeCache(packageNames: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            packageNames.forEach { packageName ->
                val screenTime = getUsageForApp(packageName, getToday())
                screenTimeCache[packageName] = screenTime
            }
            shouldReloadScreenTime.value++
        }
    } // Reloads the screen times

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
}
