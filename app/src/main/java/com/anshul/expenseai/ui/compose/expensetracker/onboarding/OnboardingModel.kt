package com.anshul.expenseai.ui.compose.expensetracker.onboarding

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class OnboardingPage(
    val title: String,
    val description: String,
    val detailedInfo: List<String>,
    val icon: ImageVector,
    val iconColor: Color,
    val gradientColors: List<Color>
)