package com.anshul.expenseai.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.anshul.expenseai.data.dao.ExpenseDao
import com.anshul.expenseai.data.entities.ExpenseEntity

@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class ExpenseAnalyserDB : RoomDatabase(){
    abstract fun expenseDao(): ExpenseDao
}