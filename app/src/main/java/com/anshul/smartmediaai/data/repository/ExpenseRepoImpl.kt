package com.anshul.smartmediaai.data.repository

import com.anshul.smartmediaai.data.entities.ExpenseEntity
import com.anshul.smartmediaai.data.model.GmailMessageResponse
import com.anshul.smartmediaai.data.model.thread.GmailThreadResponse
import kotlinx.coroutines.flow.Flow

class ExpenseRepoImpl(private val localDataSource: ExpenseLocalDataSource,
                      private val remoteDataSource: ExpenseRemoteDataSource): ExpenseRepo {
    override suspend fun insertAllExpenses(expenses: List<ExpenseEntity>) {
       localDataSource.insertAllExpenses(expenses)
    }

    override suspend fun readEmails(authHeader: String, query: String): GmailMessageResponse {
        return remoteDataSource.readEmails(authHeader, query)
    }

    override suspend fun readThreads(
        authHeader: String,
        threadId: String
    ): GmailThreadResponse {
        return remoteDataSource.readThreads(authHeader,threadId)
    }

    override suspend fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return localDataSource.getAllExpenses()
    }
}