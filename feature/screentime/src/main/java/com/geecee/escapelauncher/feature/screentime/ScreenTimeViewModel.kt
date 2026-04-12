package com.geecee.escapelauncher.feature.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.ScreenTimeRepository
import com.geecee.escapelauncher.core.data.entity.AppUsageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScreenTimeViewModel @Inject constructor(
    private val screenTimeRepository: ScreenTimeRepository
) : ViewModel() {

    private val _totalUsage = MutableStateFlow(0L)
    val totalUsage: StateFlow<Long> = _totalUsage.asStateFlow()

    private val _yesterdayTotalUsage = MutableStateFlow(0L)
    val yesterdayTotalUsage: StateFlow<Long> = _yesterdayTotalUsage.asStateFlow()

    private val _appUsageList = MutableStateFlow<List<AppUsageEntity>>(emptyList())
    val appUsageList: StateFlow<List<AppUsageEntity>> = _appUsageList.asStateFlow()

    private val _yesterdayAppUsageList = MutableStateFlow<List<AppUsageEntity>>(emptyList())
    val yesterdayAppUsageList: StateFlow<List<AppUsageEntity>> = _yesterdayAppUsageList.asStateFlow()

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

    fun getScreenTime(packageName: String): Long {
        return appUsageList.value.find { it.packageName == packageName }?.totalTime ?: 0L
    }

    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}
