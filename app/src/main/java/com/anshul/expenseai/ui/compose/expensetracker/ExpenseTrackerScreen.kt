package com.anshul.expenseai.ui.compose.expensetracker

import android.Manifest
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.anshul.expenseai.BuildConfig
import com.anshul.expenseai.data.model.ExpenseCategoryUI
import com.anshul.expenseai.data.model.StatCard
import com.anshul.expenseai.data.model.StatusBarInfo
import com.anshul.expenseai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.expenseai.ui.nav.Screen
import com.anshul.expenseai.ui.theme.MinimalDarkColors
import com.anshul.expenseai.util.constants.ExpenseConstant.EMAIL_PREFS
import com.anshul.expenseai.util.constants.ExpenseConstant.EXPENSE_SHARED_PREFS
import com.anshul.expenseai.util.constants.ExpenseConstant.FIRST_GMAIL_SIGN_IN_PREF
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.orbitmvi.orbit.compose.collectSideEffect
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "ExpenseTrackerMinimalDark"




// Helper function to map category names to colors and icons
fun getCategoryColorAndIcon(categoryName: String): Pair<Color, ImageVector> {
    return when (categoryName.lowercase()) {
        "shopping", "shop" -> MinimalDarkColors.CategoryIndigo to Icons.Default.ShoppingCart
        "food", "restaurant", "dining" -> MinimalDarkColors.CategoryRed to Icons.Default.Restaurant
        "travel", "transport", "transportation" -> MinimalDarkColors.CategoryOrange to Icons.Default.Flight
        "bills", "utilities" -> MinimalDarkColors.CategoryPurple to Icons.Default.Receipt
        "entertainment", "fun" -> MinimalDarkColors.CategoryPink to Icons.Default.Movie
        "health", "healthcare" -> MinimalDarkColors.CategoryGreen to Icons.Default.Favorite
        "education" -> MinimalDarkColors.CategoryBlue to Icons.Default.School
        "other", "others" -> MinimalDarkColors.CategoryGreen to Icons.Default.MoreHoriz
        else -> MinimalDarkColors.CategoryYellow to Icons.Default.Category
    }
}

// Main Screen with Business Logic Integration
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    navController: NavController,
    viewModel: ExpenseTrackerViewModel = hiltViewModel(),
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sp = context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher for Location
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionResult(true)
            Log.d(TAG, "Location Permission Granted")
        } else {
            viewModel.onPermissionResult(false)
            Log.d(TAG, "Location Permission Denied")
        }
    }

    // Handle side effects
    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ExpenseTrackerSideEffect.ShowToast -> {
                Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
            }
            is ExpenseTrackerSideEffect.RequestLocationPermission -> {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Gmail consent launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.fetchGmailData(context)
        } else {
            Toast.makeText(context, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(state.gmailConsentIntent) {
        if (state.gmailConsentIntent != null) {
            launcher.launch(state.gmailConsentIntent!!)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.delete30DaysOldExpenses()
    }

    // Google Sign-In Functions
    fun handleSignInWithGoogleOption(
        context: Context,
        result: GetCredentialResponse
    ) {
        val credential = result.credential
        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)
                            .edit {
                                putString(EMAIL_PREFS, googleIdTokenCredential.id)
                            }
                        viewModel.fetchGmailData(context)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid google id token", e)
                    }
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential type")
            }
        }
    }

    fun handleSignIn(result: GetCredentialResponse) {
        val credential = result.credential
        when (credential) {
            is PublicKeyCredential -> {
                val responseJson = credential.authenticationResponseJson
            }
            is PasswordCredential -> {
                val username = credential.id
                val password = credential.password
            }
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)
                        context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)
                            .edit {
                                putString(EMAIL_PREFS, googleIdTokenCredential.id)
                            }
                        viewModel.fetchGmailData(context)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Invalid google id token", e)
                    }
                }
            }
            else -> {
                Log.e(TAG, "Unexpected credential type")
            }
        }
    }

    fun createGoogleSignInWithButton() {
        val activity = context as? Activity ?: return
        val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = BuildConfig.WEB_CLIENT_ID
        ).setNonce(UUID.randomUUID().toString())
            .build()

        val credentialManager = CredentialManager.create(activity)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    credentialManager.getCredential(
                        request = request,
                        context = activity,
                    )
                }
                handleSignInWithGoogleOption(activity, result)
            } catch (e: GetCredentialException) {
                e.printStackTrace()
            }
        }
    }

    fun createGoogleSignIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        val credentialManager = CredentialManager.create(context)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            coroutineScope {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    createGoogleSignInWithButton()
                    e.printStackTrace()
                }
            }
        }
    }

    val isFirstSignIn = remember {
        sp.getBoolean(FIRST_GMAIL_SIGN_IN_PREF, true)
    }

    var showGoogleSignInFlow by remember {
        mutableStateOf(true)
    }

    // Convert ViewModel data to UI models
    val expenseData = remember(state.nativeChart) {
        state.nativeChart.map { chartData ->
            val (color) = getCategoryColorAndIcon(chartData.name)
            ExpenseCategoryUI(
                name = chartData.name,
                percentage = chartData.percentage,
                amount = chartData.amount,
                color = color
            )
        }
    }

    val totalAmount = remember(state.expenses) {
        state.expenses.sumOf { it.amount }
    }

    val currentMonth = remember {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
    }

    val statsCards = remember(expenseData, totalAmount) {
        val highestCategory = expenseData.maxByOrNull { it.amount }
        val avgDaily = if (totalAmount > 0) totalAmount / 30 else 0.0

        listOf(
            StatCard(
                label = "Highest",
                title = highestCategory?.name ?: "N/A",
                value = if (highestCategory != null) "₹${String.format("%,.0f", highestCategory.amount)}" else "₹0",
                color = MinimalDarkColors.Indigo400
            ),
            StatCard(
                label = "Avg. Daily",
                title = "Spending",
                value = "₹${String.format("%,.0f", avgDaily)}",
                color = MinimalDarkColors.Purple400
            )
        )
    }

    val statusBarInfo = remember { StatusBarInfo() }

    // Main UI
    ExpenseTrackerMinimalDarkTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MinimalDarkColors.Gray900,
                            MinimalDarkColors.Gray800
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Mobile Status Bar
                DarkStatusBar(statusBarInfo)

                // Header with Total Balance
                DarkHeader(
                    currentMonth = currentMonth,
                    totalAmount = totalAmount,
                    isLoading = state.isLoading && expenseData.isEmpty()
                )

                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if(showGoogleSignInFlow) {
                        // Google Sign-In Button (if first time)
                        if (isFirstSignIn) {
                            GoogleSignInButtonCompose(
                                onClick = {
                                    createGoogleSignInWithButton()
                                    sp.edit { putBoolean(FIRST_GMAIL_SIGN_IN_PREF, false) }
                                    viewModel.setIsFirstTimeSignInFromGoogleButton(true)
                                }
                            )
                        } else {
                            LaunchedEffect(Unit) {
                                viewModel.firstTimeSignInOccurred {
                                    createGoogleSignIn()
                                }
                            }
                        }
                    }

                    // Loading State
                    if (state.isLoading && expenseData.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MinimalDarkColors.Indigo400)
                        }
                    }

                    // Error Message
                    state.errorMessage?.let { error ->
                        ErrorCard(error)
                    }

                    // Chart Section (only if data exists)
                    if (expenseData.isNotEmpty()) {
                        showGoogleSignInFlow = false
                        CategoryBreakdownCard(
                            expenseData = expenseData,
                            onViewTransactionsClick = {
                                navController.navigate(Screen.ExpenseDetails.route)
                            }
                        )

                        // Stats Cards
                        StatsCardsRow(statsCards)
                    }

                    // Smart Insights Card (with loading state)
                    if (state.isRecommendationLoading) {
                        SmartInsightsLoadingCardDetailed()
                    } else if (state.recommendation?.isNotEmpty() == true) {
                        SmartInsightsCard(recommendation = state.recommendation)
                    }

                    // Bottom Spacer
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Bottom Navigation Indicator
            DarkBottomIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun ErrorCard(error: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MinimalDarkColors.CategoryRed.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, MinimalDarkColors.CategoryRed.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MinimalDarkColors.CategoryRed,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = error,
                color = MinimalDarkColors.Gray300,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun DarkStatusBar(info: StatusBarInfo) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MinimalDarkColors.Gray950)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = info.time,
                color = MinimalDarkColors.Gray300,
                fontSize = 14.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = "Signal",
                    tint = MinimalDarkColors.Gray300,
                    modifier = Modifier.size(16.dp)
                )
                if (info.wifiEnabled) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "WiFi",
                        tint = MinimalDarkColors.Gray300,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = MinimalDarkColors.Gray300,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DarkHeader(
    currentMonth: String,
    totalAmount: Double,
    isLoading: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MinimalDarkColors.Gray900)
            .border(
                width = 1.dp,
                color = MinimalDarkColors.Gray800,
                shape = RectangleShape
            )
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ExpenseAI",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentMonth,
                    color = MinimalDarkColors.Gray400,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MinimalDarkColors.Indigo600.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Wallet",
                    tint = MinimalDarkColors.Indigo400,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = MinimalDarkColors.Indigo600.copy(alpha = 0.5f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MinimalDarkColors.Indigo600,
                                MinimalDarkColors.Purple600
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = "Total Expenses",
                            color = MinimalDarkColors.Indigo200,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "₹${String.format("%,.2f", totalAmount)}",
                            color = Color.White,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Text(
                            text = "Last 30 days",
                            color = MinimalDarkColors.Indigo200,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBreakdownCard(
    expenseData: List<ExpenseCategoryUI>,
    onViewTransactionsClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MinimalDarkColors.Gray800.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MinimalDarkColors.Gray700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MinimalDarkColors.Gray800.copy(alpha = 0.5f),
                            MinimalDarkColors.Gray900.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Breakdown",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = MinimalDarkColors.Gray400,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Donut Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(256.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    modifier = Modifier
                        .size(256.dp)
                        .padding(8.dp),
                    data = expenseData.map { category ->
                        Pie(
                            label = category.name,
                            data = category.percentage.toDouble(),
                            color = category.color,
                            selectedColor = category.color.copy(alpha = 0.8f)
                        )
                    },
                    onPieClick = { pie ->
                      //  Log.d(TAG, "Clicked on: ${pie.label}")
                    },
                    selectedScale = 1.1f,
                    scaleAnimEnterSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    colorAnimEnterSpec = tween(300),
                    colorAnimExitSpec = tween(300),
                    scaleAnimExitSpec = tween(300),
                    spaceDegreeAnimExitSpec = tween(300),
                    style = Pie.Style.Fill,
                    spaceDegree = 4f,
                    selectedPaddingDegree = 0f
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category List
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                expenseData.forEach { category ->
                    CategoryListItem(category)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // View All Transactions Button
            DarkGradientButton(
                text = "View All Transactions",
                onClick = onViewTransactionsClick
            )
        }
    }
}

@Composable
fun CategoryListItem(category: ExpenseCategoryUI) {
    var isHovered by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isHovered = !isHovered },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHovered) {
                MinimalDarkColors.Gray900.copy(alpha = 0.7f)
            } else {
                MinimalDarkColors.Gray900.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(category.color)
                )
                Text(
                    text = category.name,
                    color = MinimalDarkColors.Gray300,
                    fontSize = 14.sp
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${category.percentage.toInt()}%",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MinimalDarkColors.Gray500,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun DarkGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = MinimalDarkColors.Indigo600.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MinimalDarkColors.Indigo600,
                            MinimalDarkColors.Purple600
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun StatsCardsRow(statsCards: List<StatCard>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        statsCards.forEach { stat ->
            StatsCard(
                stat = stat,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatsCard(
    stat: StatCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MinimalDarkColors.Gray800.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MinimalDarkColors.Gray700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MinimalDarkColors.Gray800.copy(alpha = 0.5f),
                            MinimalDarkColors.Gray900.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stat.label,
                color = MinimalDarkColors.Gray400,
                fontSize = 11.sp
            )
            Text(
                text = stat.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stat.value,
                color = stat.color,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun DarkBottomIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(128.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MinimalDarkColors.Gray700)
    )
}

@Composable
fun ExpenseTrackerMinimalDarkTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = MinimalDarkColors.Indigo600,
            secondary = MinimalDarkColors.Purple600,
            background = MinimalDarkColors.Gray900,
            surface = MinimalDarkColors.Gray800,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

// Preview
@Preview(
    name = "Minimal Dark Integrated - Full Screen",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun ExpenseTrackerMinimalDarkIntegratedPreview() {
    val context = LocalContext.current
    ExpenseTrackerScreen(
        navController = NavHostController(context)
    )
}