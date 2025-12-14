package com.anshul.expenseai.ui.compose.expensetracker.state

import android.content.Intent
import com.anshul.expenseai.data.model.ExpenseCategoryUI
import com.anshul.expenseai.data.model.MessagesItem
import com.anshul.expenseai.ui.compose.expensetracker.bottomsheet.GoogleSignInBottomSheet

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
    val recommendation: String? = null,
    val nativeChart: List<ExpenseCategoryUI> = emptyList(),
    val isGmailConsentNeeded: Boolean = false,
    val gmailConsentIntent: Intent? = null,
    val gmailMessagesId: List<MessagesItem> = emptyList(),
    val isRecommendationLoading : Boolean = false,
    val showGmailBottomSheet: Boolean = false,
    val showGmailSignInFlow: Boolean = false,
    val activeSheet: GoogleSignInBottomSheet = GoogleSignInBottomSheet.None,
)

sealed class ExpenseTrackerSideEffect {
    data class ShowToast(val message: String) : ExpenseTrackerSideEffect()
    object RequestLocationPermission : ExpenseTrackerSideEffect()
    object RequestSMSPermission: ExpenseTrackerSideEffect()
    object ShowGmailBottomSheet: ExpenseTrackerSideEffect()
    object SkipGmailSignInFlow: ExpenseTrackerSideEffect()
}


