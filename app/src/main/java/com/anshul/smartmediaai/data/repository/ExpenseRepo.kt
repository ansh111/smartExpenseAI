package com.anshul.smartmediaai.data.repository

import com.anshul.smartmediaai.data.entities.ExpenseEntity
import com.anshul.smartmediaai.data.model.GmailMessageResponse
import com.anshul.smartmediaai.data.model.thread.GmailThreadResponse
import kotlinx.coroutines.flow.Flow

interface ExpenseRepo {

    suspend fun getAllExpenses(): Flow<List<ExpenseEntity>>
    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)
    suspend fun  readEmails(authHeader: String, query: String): GmailMessageResponse
    suspend fun  readThreads(authHeader: String, threadId: String): GmailThreadResponse
}