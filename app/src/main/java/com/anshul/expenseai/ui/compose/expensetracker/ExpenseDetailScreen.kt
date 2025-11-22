package com.anshul.expenseai.ui.compose.expensetracker

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.anshul.expenseai.ui.compose.expensetracker.ExpenseTrackerMinimalDarkTheme
import com.anshul.expenseai.ui.compose.expensetracker.ExpenseTrackerViewModel
import com.anshul.expenseai.ui.compose.expensetracker.getCategoryColorAndIcon
import com.anshul.expenseai.ui.compose.expensetracker.state.ExpenseItem
import com.anshul.expenseai.ui.theme.MinimalDarkColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavController,
    viewModel: ExpenseTrackerViewModel
) {
    val state by viewModel.container.stateFlow.collectAsStateWithLifecycle()

    val totalAmount = remember(state.expenses) {
        state.expenses.sumOf { it.amount }
    }

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
                // Custom Top Bar
                ExpenseDetailsTopBar(
                    onBackClick = { navController.popBackStack() },
                    totalExpenses = state.expenses.size,
                    totalAmount = totalAmount
                )

                // Content
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MinimalDarkColors.Indigo400)
                    }
                } else if (state.expenses.isEmpty()) {
                    EmptyExpensesState()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(
                            items = state.expenses,
                            key = { it.messageId }
                        ) { expense ->
                            ExpenseCard(expense = expense)
                        }

                        // Bottom spacer
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ExpenseDetailsTopBar(
    onBackClick: () -> Unit,
    totalExpenses: Int,
    totalAmount: Double
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
    ) {
        // Status bar placeholder
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .background(MinimalDarkColors.Gray950)
        )

        // Header content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MinimalDarkColors.Gray800)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Expense Details",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalExpenses transactions",
                        color = MinimalDarkColors.Gray400,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // Filter icon
                IconButton(
                    onClick = { /* TODO: Add filter functionality */ },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MinimalDarkColors.Indigo600.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = MinimalDarkColors.Indigo400
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Amount",
                                color = MinimalDarkColors.Indigo200,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "₹${String.format("%,.2f", totalAmount)}",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(expense: ExpenseItem) {
    var isExpanded by remember { mutableStateOf(false) }

    // Get category color and icon
    val (color, icon) = getCategoryColorAndIcon(expense.category.toString())

    // Format date
    val formattedDate = remember(expense.date) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(expense.date)
            date?.let { outputFormat.format(it) } ?: expense.date
        } catch (e: Exception) {
            expense.date
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MinimalDarkColors.Gray800.copy(alpha = 0.6f)
        ),
        border = BorderStroke(1.dp, MinimalDarkColors.Gray700)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MinimalDarkColors.Gray800.copy(alpha = 0.6f),
                            MinimalDarkColors.Gray900.copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Icon and details
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = expense.category,
                            tint = color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Merchant name and details
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = expense.merchant,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            // Category badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(color.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = expense.category,
                                    color = color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = "•",
                                color = MinimalDarkColors.Gray500,
                                fontSize = 11.sp
                            )

                            Text(
                                text = formattedDate,
                                color = MinimalDarkColors.Gray400,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Right side - Amount
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "₹${String.format("%,.2f", expense.amount)}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MinimalDarkColors.Gray500,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(top = 4.dp)
                    )
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(
                        color = MinimalDarkColors.Gray700,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExpenseDetailRow(
                        label = "Category",
                        value = expense.category,
                        icon = Icons.Default.Category
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExpenseDetailRow(
                        label = "Date",
                        value = formattedDate,
                        icon = Icons.Default.CalendarToday
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExpenseDetailRow(
                        label = "Merchant",
                        value = expense.merchant,
                        icon = Icons.Default.Store
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailRow(
    label: String,
    value: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MinimalDarkColors.Gray500,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = MinimalDarkColors.Gray400,
                fontSize = 13.sp
            )
        }

        Text(
            text = value,
            color = MinimalDarkColors.Gray300,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyExpensesState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MinimalDarkColors.Gray800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = MinimalDarkColors.Gray600,
                    modifier = Modifier.size(56.dp)
                )
            }

            Text(
                text = "No Expenses Yet",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your expense transactions will appear here",
                color = MinimalDarkColors.Gray400,
                fontSize = 14.sp
            )
        }
    }
}