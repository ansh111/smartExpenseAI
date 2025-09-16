package com.anshul.smartmediaai.core.wm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anshul.smartmediaai.data.repository.ReadSmsRepo
import androidx.core.content.edit

class SmsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val readSmsRepo: ReadSmsRepo

): CoroutineWorker(appContext,workerParams) {

    companion object {
        const val TAG = "SmsSyncWorker"
        const val LAST_SYNC_TIME = "last_sync_time"
    }


    override suspend fun doWork(): Result {

        try{
            val sp = applicationContext.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
            val lastSyncTimestamp = sp.getLong(LAST_SYNC_TIME,0L)
            readSmsRepo.readSms(lastSyncTimestamp)
            sp.edit { putLong(LAST_SYNC_TIME, System.currentTimeMillis()) }

        } catch (e : Exception){
            e.printStackTrace()
        }

        return Result.success()

    }
}