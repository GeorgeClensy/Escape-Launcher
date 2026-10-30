package com.geecee.escapelauncher.feature.appslist

import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.formatScreenTime
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.repository.settings.*
import com.geecee.escapelauncher.core.domain.search.SearchAppsUseCase
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

data class AppListItem(
    val app: InstalledApp,
    val formattedTime: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AppsListViewModel @Inject constructor(
    appearanceRepository: AppearanceRepository,
    searchSettingsRepository: SearchSettingsRepository,
    launcherBehaviorRepository: LauncherBehaviorRepository,
    screenTimeSettingsRepository: ScreenTimeSettingsRepository,
    searchAppsUseCase: SearchAppsUseCase,
    screenTimeRepository: ScreenTimeRepository
) : ViewModel() {
    // Settings
    val showScreenTimeApp = screenTimeSettingsRepository.showScreenTimeApp
    val appsAlignment = appearanceRepository.appsAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }
    val showSearchBox = searchSettingsRepository.showSearchBox
    val searchAutoOpen = searchSettingsRepository.searchAutoOpen
    val automaticallyOpenAppsInSearch = searchSettingsRepository.automaticallyOpenAppsInSearch
    val hiddenAppsInSearch = searchSettingsRepository.showHiddenAppsInSearch
    val hapticFeedBackEnabled = launcherBehaviorRepository.hapticFeedBackEnabled
    val showWallpaper = appearanceRepository.showWallpaper

    // Search
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _searchExpanded = MutableStateFlow(false)
    val searchExpanded: StateFlow<Boolean> = _searchExpanded.asStateFlow()
    fun onSearchTextChanged(query: String) {
        if (_searchExpanded.value) {
            _searchText.value = query
        }
    }
    fun onSearchExpandedChanged(expanded: Boolean) {
        _searchExpanded.value = expanded
        if (!_searchExpanded.value) {
            _searchText.value = ""
        }
    }

    // Apps
    val apps: StateFlow<List<InstalledApp>> = searchAppsUseCase(_searchText, hiddenAppsInSearch)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val todayFlow: Flow<String> = flow {
        while (true) {
            val now = Date()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
            emit(today)

            val calendar = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val delayMs = calendar.timeInMillis - System.currentTimeMillis()
            delay((delayMs + 1000).milliseconds)
        }
    }.distinctUntilChanged()

    private val todayUsageMap: Flow<Map<String, Long>> = todayFlow.flatMapLatest { today ->
        screenTimeRepository.getScreenTimeListSortedFlow(today).map { list ->
            list.associate { it.packageName to it.totalTime }
        }
    }

    // Combined UI Model list to avoid LazyColumn recomposition explosion
    val appListItems: StateFlow<List<AppListItem>> = combine(
        apps,
        showScreenTimeApp,
        todayUsageMap
    ) { appsList, showScreenTime, usageMap ->
        appsList.map { app ->
            val formattedTime = if (showScreenTime) {
                val time = usageMap[app.packageName] ?: 0L
                formatScreenTime(time)
            } else {
                null
            }
            AppListItem(
                app = app,
                formattedTime = formattedTime
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
