package com.geecee.escapelauncher.feature.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.screentime.GetAppUsageUiListUseCase
import com.geecee.escapelauncher.core.model.AppUsageUiModel
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
    private val screenTimeRepository: ScreenTimeRepository,
    private val getAppUsageUiListUseCase: GetAppUsageUiListUseCase
) : ViewModel() {

    private val _totalUsage = MutableStateFlow(0L)
    val totalUsage: StateFlow<Long> = _totalUsage.asStateFlow()

    private val _yesterdayTotalUsage = MutableStateFlow(0L)
    val yesterdayTotalUsage: StateFlow<Long> = _yesterdayTotalUsage.asStateFlow()

    private val _appUsageUiList = MutableStateFlow<List<AppUsageUiModel>>(emptyList())
    val appUsageUiList: StateFlow<List<AppUsageUiModel>> = _appUsageUiList.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val yesterday = getYesterdayDateString()
        
        viewModelScope.launch {
            // Load Today Total
            _totalUsage.value = screenTimeRepository.getTotalUsageForDate(today)
            
            // Load Yesterday Total
            _yesterdayTotalUsage.value = screenTimeRepository.getTotalUsageForDate(yesterday)

            // Load UI List via UseCase
            _appUsageUiList.value = getAppUsageUiListUseCase(today, yesterday)
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
        return _appUsageUiList.value.find { it.packageName == packageName }?.totalTime ?: 0L
    }

    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}
