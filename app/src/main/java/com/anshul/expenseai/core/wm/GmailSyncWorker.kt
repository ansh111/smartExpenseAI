package com.anshul.expenseai.core.wm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anshul.expenseai.data.repository.GmailRepo
import com.anshul.expenseai.ui.compose.expensetracker.ExpenseTrackerViewModel.Companion.LAST_SYNC_TIME
import com.anshul.expenseai.util.constants.ExpenseConstant.EXPENSE_SHARED_PREFS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject
@HiltWorker
class GmailSyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParameters: WorkerParameters ): CoroutineWorker(appContext,workerParameters) {

    @Inject
    lateinit var repo: GmailRepo

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