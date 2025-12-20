package com.anshul.expenseai.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.anshul.expenseai.data.dao.ExpenseDao
import com.anshul.expenseai.data.entities.ExpenseEntity
import com.anshul.expenseai.ui.compose.expensetracker.UserOnboardingInfo
import com.anshul.expenseai.util.constants.ExpenseConstant.ONBOARDED_USER_INFO
import com.anshul.expenseai.util.constants.ExpenseConstant.SHOW_ONBOARDING
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class ExpenseLocalDataSource(
    private val dao: ExpenseDao,
    private val prefs: SharedPreferences,
    private val gson: Gson
) {
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

    fun shouldShowOnboarding() = prefs.getBoolean(SHOW_ONBOARDING,
        true)

    fun saveUser(userInfo: UserOnboardingInfo) {
        prefs.edit {
            putString(ONBOARDED_USER_INFO, gson.toJson(userInfo))
            putBoolean(SHOW_ONBOARDING, false)
        }
    }

    fun getUser(): UserOnboardingInfo? = prefs.getString(ONBOARDED_USER_INFO,"")?.let {
        gson.fromJson(it, UserOnboardingInfo::class.java)
    }



}