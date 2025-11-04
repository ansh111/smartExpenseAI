package com.anshul.smartmediaai

import android.app.Application
import android.util.Log.VERBOSE
import android.webkit.WebView
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.anshul.smartmediaai.core.wm.CleanUpWorker
import com.anshul.smartmediaai.core.wm.GmailSyncWorker
import com.google.firebase.Firebase
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SmartMediaAIApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        scheduleWorkManager()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(VERBOSE)
            .build()



    fun scheduleWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<GmailSyncWorker>(
            1, TimeUnit.DAYS // Run once every 24 hours
        )
            .setInitialDelay(15, TimeUnit.MINUTES) // optional delay before first run
            .addTag("gmail_sync_worker") // optional tag to identify it
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "gmail_sync_worker",
            ExistingPeriodicWorkPolicy.KEEP, // keep existing if already scheduled
            workRequest
        )


        val cleanupRequest = PeriodicWorkRequestBuilder<CleanUpWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "cleanup_old_expenses",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest
            )

    }
}