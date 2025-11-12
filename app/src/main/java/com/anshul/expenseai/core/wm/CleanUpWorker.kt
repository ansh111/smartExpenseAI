package com.anshul.expenseai.core.wm

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anshul.expenseai.data.repository.ExpenseRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class CleanUpWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {


    @Inject
    lateinit var expenseRepo: ExpenseRepo
    override suspend fun doWork(): Result {
        try {
            expenseRepo.delete30DaysOldExpenses()
        } catch ( e: Exception){
           return Result.failure()
        }
        return Result.success()
    }
}
