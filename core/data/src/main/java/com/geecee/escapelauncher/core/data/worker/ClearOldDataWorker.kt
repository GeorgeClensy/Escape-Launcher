package com.geecee.escapelauncher.core.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.geecee.escapelauncher.core.data.repository.ScreenTimeRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

@HiltWorker
class ClearOldDataWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val screenTimeRepository: ScreenTimeRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            screenTimeRepository.clearOldData()
            Log.d("ClearOldDataWorker", "Ran ClearDatWorker")
            Result.success()
        } catch (e: Exception) {
            Log.e("ClearOldDataWorker", "Error clearing old data: ${e.message}")
            Result.failure()
        }
    }

    companion object {
        fun scheduleDailyCleanup(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<ClearOldDataWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(calculateMidnightDelay(), TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "ClearOldDataWorker",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }

        private fun calculateMidnightDelay(): Long {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis - now
        }
    }
}
