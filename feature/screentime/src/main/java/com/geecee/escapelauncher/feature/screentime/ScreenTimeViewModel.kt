package com.geecee.escapelauncher.feature.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.repository.AppsRepository
import com.geecee.escapelauncher.core.model.AppUsage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScreenTimeViewModel @Inject constructor(
    private val screenTimeRepository: ScreenTimeRepository,
    private val appsRepository: AppsRepository
) : ViewModel() {

    private val _totalUsage = MutableStateFlow(0L)
    val totalUsage: StateFlow<Long> = _totalUsage.asStateFlow()

    private val _yesterdayTotalUsage = MutableStateFlow(0L)
    val yesterdayTotalUsage: StateFlow<Long> = _yesterdayTotalUsage.asStateFlow()

    private val _appUsageList = MutableStateFlow<List<AppUsage>>(emptyList())
    val appUsageList: StateFlow<List<AppUsage>> = _appUsageList.asStateFlow()

    private val _yesterdayAppUsageList = MutableStateFlow<List<AppUsage>>(emptyList())
    val yesterdayAppUsageList: StateFlow<List<AppUsage>> = _yesterdayAppUsageList.asStateFlow()

    val appUsageUiList: StateFlow<List<AppUsageUiModel>> = combine(
        appUsageList,
        yesterdayAppUsageList
    ) { today, yesterday ->
        today.map { appScreenTime ->
            val yesterdayAppUsage = yesterday.find { it.packageName == appScreenTime.packageName }
            val usageIncreased = appScreenTime.totalTime > (yesterdayAppUsage?.totalTime ?: 0L)
            val appName = appsRepository.getAppNameFromPackageName(appScreenTime.packageName)

            AppUsageUiModel(
                packageName = appScreenTime.packageName,
                appName = appName,
                totalTime = appScreenTime.totalTime,
                usageIncreased = usageIncreased
            )
        }.filter { it.appName != "null" }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadData()
    }

    fun loadData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = getYesterdayDateString()
        
        viewModelScope.launch {
            // Load Today
            _totalUsage.value = screenTimeRepository.getTotalUsageForDate(today)
            _appUsageList.value = screenTimeRepository.getScreenTimeListSorted(today)
            
            // Load Yesterday
            _yesterdayTotalUsage.value = screenTimeRepository.getTotalUsageForDate(yesterday)
            _yesterdayAppUsageList.value = screenTimeRepository.getScreenTimeListSorted(yesterday)
        }
    }

    fun onAppOpened(packageName: String) {
        screenTimeRepository.onAppOpened(packageName)
    }

    suspend fun onAppClosed(packageName: String) {
        screenTimeRepository.onAppClosed(packageName)
        loadData() // Refresh after close
    }

    fun hasActiveSession(): Boolean {
        return screenTimeRepository.hasActiveSession()
    }

    fun getActiveSessionPackageName(): String? {
        return screenTimeRepository.getActiveSessionPackageName()
    }

    fun getScreenTime(packageName: String): Long {
        return appUsageList.value.find { it.packageName == packageName }?.totalTime ?: 0L
    }

    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}
