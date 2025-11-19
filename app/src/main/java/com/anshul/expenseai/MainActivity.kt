package com.anshul.expenseai

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.anshul.expenseai.ui.compose.expensetracker.onboarding.ExpenseTrackerWithOnboardingApp
import com.anshul.expenseai.ui.nav.ExpenseNavigation
import com.anshul.expenseai.ui.theme.SmartMediaAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //createGoogleSignIn()
       //createGoogleSignInWithButton()
        enableEdgeToEdge()
        setContent {
            /*SmartMediaAITheme {
                val navController = rememberNavController()
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background
                ){ innerPadding ->
                    ExpenseNavigation(
                        navController = navController,
                        modifier = Modifier
                    )
                   // ExpenseTrackerScreen(modifier = Modifier.padding(innerPadding))
                   // VideoSummarisationScreen(modifier = Modifier.padding(innerPadding))
                }
            }*/
            ExpenseTrackerWithOnboardingApp()
        }
    }


}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartMediaAITheme {
        Greeting("Android")
    }
}