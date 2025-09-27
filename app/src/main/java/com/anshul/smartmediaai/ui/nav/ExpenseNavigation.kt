package com.anshul.smartmediaai.ui.nav

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.internal.composableLambda
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerScreen
import androidx.navigation.compose.composable
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseDetailsScreen
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerViewModel

@Composable
fun ExpenseNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
){
    NavHost(
        navController = navController,
        startDestination = Screen.ExpenseTracker.route,
        modifier = modifier
    ) {
        composable(Screen.ExpenseTracker.route) {
            ExpenseTrackerScreen(navController)
        }
        composable(Screen.ExpenseDetails.route) { backStackEntry ->

            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.ExpenseTracker.route)
            }
            val viewModel: ExpenseTrackerViewModel = hiltViewModel(parentEntry)
            ExpenseDetailsScreen(navController,viewModel)

        }
    }
}