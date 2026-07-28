package com.example.icaibatchchecker

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.icaibatchchecker.notification.NotificationHelper
import com.example.icaibatchchecker.worker.BatchCheckWorker
import java.util.concurrent.TimeUnit

class IcaiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
        schedulePeriodicCheck()
    }

    private fun schedulePeriodicCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<BatchCheckWorker>(
            10, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "batch_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
