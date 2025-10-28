package com.anshul.smartmediaai.core.wm

import android.content.Context
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anshul.smartmediaai.data.repository.GmailRepo
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerViewModel.Companion.LAST_SYNC_TIME
import com.anshul.smartmediaai.util.constants.ExpenseConstant.EXPENSE_SHARED_PREFS
import javax.inject.Inject

class GmailSyncWorker @Inject constructor(private val appContext: Context, workerParameters: WorkerParameters, val repo: GmailRepo ): CoroutineWorker(appContext,workerParameters) {

    override suspend fun doWork(): Result {
       try{

           val sp = applicationContext.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)
           val lastSyncTimestamp = sp.getLong(LAST_SYNC_TIME,0L)
           repo.readMails(appContext, lastSyncTimestamp)

       } catch (e : Exception){
           return Result.failure()
       }
        return  Result.success()
    }
}