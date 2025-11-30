package com.anshul.expenseai.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anshul.expenseai.ui.compose.expensetracker.ExpenseDetailScreen
import com.anshul.expenseai.ui.compose.expensetracker.ExpenseTrackerScreen
import com.anshul.expenseai.ui.compose.expensetracker.ExpenseTrackerViewModel

@Composable
fun ExpenseNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
){
    NavHost(
        navController = navController,
        startDestination = Screen.ExpenseTracker.route,
        modifier = modifier
    ) {
        composable(Screen.ExpenseTracker.route) {
            ExpenseTrackerScreen(navController)
        }
        composable(
            Screen.ExpenseDetails.route,
            arguments = listOf(navArgument("categoryName"){ type = NavType.StringType }))
             { backStackEntry ->
                val categoryName = backStackEntry.arguments?.getString("categoryName")
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.ExpenseTracker.route)
                }
                val viewModel: ExpenseTrackerViewModel = hiltViewModel(parentEntry)
                ExpenseDetailScreen(navController, viewModel,categoryName)

            }
    }
}