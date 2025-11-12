package com.anshul.expenseai.ui.compose.expensetracker.state

import android.content.Intent
import com.anshul.expenseai.data.model.MessagesItem

data class ExpenseItem( // Define your refined expense data structure
    val merchant: String,
    val amount: Double,
    val date: String,
    val category: String,
    val messageId: String
)

data class ExpenseTrackerState(
    val isLoading: Boolean = false,
    val expenses: List<ExpenseItem> = emptyList(),
    val errorMessage: String? = null,
    val permissionGranted: Boolean = false,
    val recommendation: String? = null,
    val nativeChart: Map<String, Double> = emptyMap(),
    val isGmailConsentNeeded: Boolean = false,
    val gmailConsentIntent: Intent? = null,
    val gmailMessagesId: List<MessagesItem> = emptyList(),
    val isRecommendationLoading : Boolean = false
)

sealed class ExpenseTrackerSideEffect {
    data class ShowToast(val message: String) : ExpenseTrackerSideEffect()
    object RequestSmsPermission : ExpenseTrackerSideEffect()
}