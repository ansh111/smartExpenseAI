package com.anshul.smartmediaai.ui.compose.expensetracker

import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavController
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect.ShowToast
import com.anshul.smartmediaai.ui.nav.Screen
import com.anshul.smartmediaai.ui.theme.PrimaryBlue
import org.orbitmvi.orbit.compose.collectSideEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    navController: NavController,
    viewModel: ExpenseTrackerViewModel = hiltViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionResult(true)
            Log.d("SMS", "Permission Granted. Proceed with reading messages.")
        } else {
            viewModel.onPermissionResult(false)
            Log.d("SMS", "Permission Denied. Proceed without reading messages.")
        }
    }

    viewModel.collectSideEffect { sideEffect ->
        when(sideEffect){
            is ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }

            is ExpenseTrackerSideEffect.RequestSmsPermission -> {
                permissionLauncher.launch(Manifest.permission.READ_SMS)
            }
        }

    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Expense Tracker") },
                colors = topAppBarColors(
                containerColor = PrimaryBlue,
                titleContentColor = Color.White,
            ))
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Button(onClick = { viewModel.scanSmsForExpenses() }) {
                    Text("Scan SMS for Expenses")
                }
            }

            item {
                GoogleSignInButtonCompose( {
                    viewModel.createGoogleSignInWithButton()
                })
            }

            if (state.isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage?.let {
                item {
                    Text("Error: $it", color = MaterialTheme.colorScheme.error)
                }
            }

            if(state.nativeChart.isNotEmpty()){
                item {
                    Text("Expense Categories Chart:", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExpenseNativeChart(viewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            if (state.expenses.isNotEmpty()) {
                item {
                    Button( onClick = {navController.navigate(Screen.ExpenseDetails.route)}) {
                        Text("View Expenses")
                    }
                }

            }

            if (state.recommendation?.isNotEmpty() == true) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = AbsoluteRoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Header Row with Icon + Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp, // 💡 Material icon
                                    contentDescription = "Recommendation",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recommendation",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Body Text
                            Text(
                                text = state.recommendation ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ExpenseTrackerScreenPreview() {
    val context = LocalContext.current
    ExpenseTrackerScreen(
        navController = NavHostController(context)
    )
}

