package com.anshul.expenseai.ui.compose.expensetracker.onboarding

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController

import com.anshul.expenseai.ui.nav.ExpenseNavigation
import com.anshul.expenseai.ui.theme.SmartMediaAITheme
import com.anshul.expenseai.util.constants.ExpenseConstant.EXPENSE_SHARED_PREFS
import androidx.core.content.edit
import com.anshul.expenseai.util.constants.ExpenseConstant.SHOW_ONBOARDING
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun ExpenseTrackerWithOnboardingApp() {
    val context = LocalContext.current
    val sp = context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)
    var showOnboarding by remember {
        mutableStateOf(sp.getBoolean(SHOW_ONBOARDING, true))
    }
    val navController = rememberNavController()
    SmartMediaAITheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showOnboarding) {
                OnboardingFlow(
                    onComplete = {
                        sp.edit { putBoolean(SHOW_ONBOARDING, false) }
                        showOnboarding = false
                    }
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