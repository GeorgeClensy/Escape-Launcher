package com.geecee.escapelauncher.feature.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.screentime.GetAppUsageUiListUseCase
import com.geecee.escapelauncher.core.model.AppUsageUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScreenTimeViewModel @Inject constructor(
    private val screenTimeRepository: ScreenTimeRepository,
    getAppUsageUiListUseCase: GetAppUsageUiListUseCase
) : ViewModel() {

    private val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    private val yesterday = getYesterdayDateString()

    val totalUsage: StateFlow<Long> = screenTimeRepository.getTotalUsageForDateFlow(today)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val yesterdayTotalUsage: StateFlow<Long> = screenTimeRepository.getTotalUsageForDateFlow(yesterday)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val appUsageUiList: StateFlow<List<AppUsageUiModel>> = getAppUsageUiListUseCase(today, yesterday)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onAppOpened(packageName: String) {
        screenTimeRepository.onAppOpened(packageName)
    }

    suspend fun onAppClosed(packageName: String) {
        screenTimeRepository.onAppClosed(packageName)
    }

    fun hasActiveSession(): Boolean {
        return screenTimeRepository.hasActiveSession()
    }

    fun getActiveSessionPackageName(): String? {
        return screenTimeRepository.getActiveSessionPackageName()
    }

    fun getScreenTime(packageName: String): Long {
        return appUsageUiList.value.find { it.packageName == packageName }?.totalTime ?: 0L
    }

    private fun getYesterdayDateString(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }
}
