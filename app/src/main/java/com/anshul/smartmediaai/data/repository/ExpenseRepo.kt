package com.anshul.smartmediaai.data.repository

import com.anshul.smartmediaai.data.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

interface ExpenseRepo {

    suspend fun getAllExpenses(): Flow<List<ExpenseEntity>>
    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)
}