package com.anshul.expenseai.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anshul.expenseai.data.entities.ExpenseEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity):Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpense(expense: List<ExpenseEntity>)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE messageId = :id")
    fun getExpenseById(id: Long): Flow<ExpenseEntity>
    @Query("SELECT * FROM expenses WHERE date(date) >= date('now', '-30 day') ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("DELETE FROM expenses")
    suspend fun clearAllExpenses()

    @Query("DELETE FROM expenses WHERE date < :thresholdDate")
    suspend fun deleteOldExpenses(thresholdDate: Long)

}