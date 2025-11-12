package com.anshul.expenseai.core.wm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anshul.expenseai.data.repository.ReadSmsRepo
import androidx.core.content.edit
import com.anshul.expenseai.ui.compose.expensetracker.ExpenseTrackerViewModel.Companion.LAST_SYNC_TIME

class SmsSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val readSmsRepo: ReadSmsRepo

): CoroutineWorker(appContext,workerParams) {

    companion object {
        const val TAG = "SmsSyncWorker"

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