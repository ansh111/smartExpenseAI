package com.anshul.smartmediaai.data.repository

import com.anshul.smartmediaai.data.dao.ExpenseDao
import com.anshul.smartmediaai.data.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow

class ExpenseLocalDataSource(private val dao: ExpenseDao) {
    suspend fun insertExpense(expense: ExpenseEntity) {
        dao.insertExpense(expense)
    }

    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>) {
        dao.insertAllExpense(expenses)

    }

    suspend fun  deleteExpenseByID(expense: ExpenseEntity) {
     dao.deleteExpense(expense = expense)
    }

    suspend fun clearExpense() {
        dao.clearAllExpenses()
    }

     fun getAllExpenses() : Flow<List<ExpenseEntity>> {
       return dao.getAllExpenses()
    }

     fun getExpenseById(id: Long): Flow<ExpenseEntity> {
      return  dao.getExpenseById(id)
    }

    suspend fun deleteOldExpenses(thresholdDate: Long) {
        dao.deleteOldExpenses(thresholdDate)
    }

}