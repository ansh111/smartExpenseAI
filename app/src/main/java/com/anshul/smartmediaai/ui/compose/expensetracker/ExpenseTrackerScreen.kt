package com.anshul.smartmediaai.ui.compose.expensetracker

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.tooling.preview.Preview
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect.ShowToast
import org.orbitmvi.orbit.compose.collectSideEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    viewModel: ExpenseTrackerViewModel = hiltViewModel(),
    modifier: Modifier
) {
    val state by viewModel.container.stateFlow.collectAsState()
    val context = LocalContext.current

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect){
            is ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }

            else -> {}
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Expense Tracker") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { viewModel.scanSmsForExpenses() }) {
                Text("Scan SMS for Expenses")
            }

            if (state.isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }


            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
            }

            // TODO: Display refined expense data from state.expenses
            // e.g., using a LazyColumn
            if (state.expenses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Detected Expenses:", style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing between items
                ) {
                    items(state.expenses, key = { expense -> expense.amount }) { expense ->
                        ExpenseCard(expense = expense)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpenseTrackerScreenPreview() {
    // You might want to provide a mock ViewModel for previews
    ExpenseTrackerScreen(modifier = Modifier.fillMaxSize())
}
