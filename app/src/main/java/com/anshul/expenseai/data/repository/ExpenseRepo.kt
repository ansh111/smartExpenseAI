package com.anshul.expenseai.data.repository

import com.anshul.expenseai.data.entities.ExpenseEntity
import com.anshul.expenseai.data.model.GmailMessageResponse
import com.anshul.expenseai.data.model.thread.GmailThreadResponse
import kotlinx.coroutines.flow.Flow

interface ExpenseRepo {

    suspend fun getAllExpenses(): Flow<List<ExpenseEntity>>
    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)
    suspend fun  readEmails(authHeader: String, query: String): GmailMessageResponse
    suspend fun  readThreads(authHeader: String, threadId: String): GmailThreadResponse
    suspend fun  delete30DaysOldExpenses()
}