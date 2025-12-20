package com.anshul.expenseai.ui.nav

// In a file like navigation/Screen.kt or similar
sealed class Screen(val route: String) {
    object ExpenseTracker : Screen("expense_tracker_screen")
    object ExpenseDetails : Screen("expense_details_screen/{categoryName}") {
        // Helper function to create the route with the actual category
        fun createRoute(categoryName: String) = "expense_details_screen/$categoryName"
    }

    object ExpenseGetStarted: Screen("expense_get_started_screen")
    object ExpenseOnBoarding:Screen("expense_onboarding_screen")
}
