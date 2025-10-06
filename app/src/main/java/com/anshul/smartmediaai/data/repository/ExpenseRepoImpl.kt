package com.anshul.smartmediaai.data.repository

import com.anshul.smartmediaai.data.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepoImpl(private val localDataSource: ExpenseLocalDataSource,
                      private val remoteDataSource: ExpenseRemoteDataSource): ExpenseRepo {
    override suspend fun insertAllExpenses(expenses: List<ExpenseEntity>) {
       localDataSource.insertAllExpenses(expenses)
    }

    override suspend fun readEmails(authHeader: String, url: String) {
        remoteDataSource.readEmails(authHeader, url)
    }

    override suspend fun getAllExpenses(): Flow<List<ExpenseEntity>> {
        return localDataSource.getAllExpenses()
    }
}