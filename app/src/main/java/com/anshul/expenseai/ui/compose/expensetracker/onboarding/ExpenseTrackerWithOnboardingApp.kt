package com.anshul.expenseai.ui.compose.expensetracker.onboarding

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController

import com.anshul.expenseai.ui.nav.ExpenseNavigation
import com.anshul.expenseai.ui.theme.SmartMediaAITheme


@Composable
fun ExpenseTrackerWithOnboardingApp() {
    var showOnboarding by remember { mutableStateOf(true) }
    val navController = rememberNavController()
    SmartMediaAITheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showOnboarding) {
                OnboardingFlow(
                    onComplete = { showOnboarding = false }
                )
            } else {
                ExpenseNavigation(
                    navController = navController,
                    modifier = Modifier
                )
            }
        }
    }
}