package com.anshul.expenseai.data.repository

import android.content.Context
import com.anshul.expenseai.ui.compose.expensetracker.state.ExpenseItem

interface GmailRepo {

    suspend fun readMails(appContext: Context, lastSyncTimestamp: Long): List<ExpenseItem>
}