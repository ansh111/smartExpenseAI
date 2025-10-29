package com.anshul.smartmediaai.core.wm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anshul.smartmediaai.data.repository.ExpenseRepo
import javax.inject.Inject


class CleanUpWorker @Inject constructor(private val context: Context, params: WorkerParameters, private val expenseRepo: ExpenseRepo
) : CoroutineWorker(context, params) {


    override suspend fun doWork(): Result {
        try {
            expenseRepo.delete30DaysOldExpenses()
        } catch ( e: Exception){
           return Result.failure()
        }
        return Result.success()
    }
}
