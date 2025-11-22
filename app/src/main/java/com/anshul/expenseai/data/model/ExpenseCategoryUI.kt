package com.anshul.expenseai.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data Models
data class ExpenseCategoryUI(
    val name: String,
    val percentage: Float,
    val amount: Double,
    val color: Color
)

data class StatusBarInfo(
    val time: String = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
    val signalStrength: Int = 4,
    val wifiEnabled: Boolean = true,
    val batteryLevel: Int = 85
)

data class StatCard(
    val label: String,
    val title: String,
    val value: String,
    val color: Color
)