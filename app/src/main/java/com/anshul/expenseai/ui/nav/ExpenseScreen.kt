package com.anshul.expenseai.ui.nav

sealed class Screen(val  route: String) {
    object ExpenseTracker :  Screen("expense_tracker_screen")
    object ExpenseDetails: Screen("expense_details_screen")

}