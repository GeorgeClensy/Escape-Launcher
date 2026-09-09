package com.geecee.escapelauncher

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EscapeApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * WorkManager configuration. The default WorkerFactory can only construct workers with a
     * (Context, WorkerParameters) constructor, so without this our @HiltWorker
     * (ClearOldDataWorker) could never be instantiated and the daily cleanup silently failed.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
