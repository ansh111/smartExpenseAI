package com.anshul.smartmediaai.data.repository

import android.content.Context
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseItem

interface GmailRepo {

    suspend fun readMails(appContext: Context, lastSyncTimestamp: Long): List<ExpenseItem>
}