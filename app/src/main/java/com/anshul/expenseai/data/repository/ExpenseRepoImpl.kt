package com.anshul.expenseai.data.repository

import com.anshul.expenseai.data.entities.ExpenseEntity
import com.anshul.expenseai.data.model.GmailMessageResponse
import com.anshul.expenseai.data.model.thread.GmailThreadResponse
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

    override suspend fun delete30DaysOldExpenses() {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        return localDataSource.deleteOldExpenses(thirtyDaysAgo)
    }

    override suspend fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return localDataSource.getAllExpenses()
    }



}