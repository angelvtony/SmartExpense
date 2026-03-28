package com.example.smartexpense

import android.app.Application

import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SmartExpenseApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        val workManager = androidx.work.WorkManager.getInstance(this)

        val weeklyRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.smartexpense.data.worker.WeeklyReportWorker>(7, java.util.concurrent.TimeUnit.DAYS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "WeeklyReportWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            weeklyRequest
        )

        val monthlyRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.smartexpense.data.worker.MonthlyReportWorker>(30, java.util.concurrent.TimeUnit.DAYS)
            .build()
        workManager.enqueueUniquePeriodicWork(
            "MonthlyReportWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            monthlyRequest
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
