package com.geecee.escapelauncher.feature.screentime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.repository.db.ScreenTimeRepository
import com.geecee.escapelauncher.core.domain.screentime.GetAppUsageUiListUseCase
import com.geecee.escapelauncher.core.model.AppUsageUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ScreenTimeViewModel @Inject constructor(
    private val screenTimeRepository: ScreenTimeRepository,
    private val getAppUsageUiListUseCase: GetAppUsageUiListUseCase
) : ViewModel() {

    private val datesFlow: Flow<Pair<String, String>> = flow {
        while (true) {
            val now = Date()
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now)
            val calendar = Calendar.getInstance()
            calendar.time = now
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            emit(today to yesterday)

            // Calculate delay until next midnight
            val nextMidnight = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val delayMs = nextMidnight.timeInMillis - System.currentTimeMillis()
            delay((delayMs + 1000).milliseconds) // 1 second buffer to ensure we've crossed into the next day
        }
    }.distinctUntilChanged()

    val totalUsage: StateFlow<Long> = datesFlow.flatMapLatest { (today, _) ->
        screenTimeRepository.getTotalUsageForDateFlow(today)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val yesterdayTotalUsage: StateFlow<Long> = datesFlow.flatMapLatest { (_, yesterday) ->
        screenTimeRepository.getTotalUsageForDateFlow(yesterday)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    val appUsageUiList: StateFlow<List<AppUsageUiModel>> = datesFlow.flatMapLatest { (today, yesterday) ->
        getAppUsageUiListUseCase(today, yesterday)
    }.stateIn(
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
}
