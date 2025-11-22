package com.anshul.expenseai.ui.compose.expensetracker

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anshul.expenseai.ui.theme.MinimalDarkColors
import kotlinx.coroutines.delay

@Composable
fun SmartInsightsCard(recommendation: String?) {
    var isVisible by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    // Trigger animation on composition
    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient offset"
    )

    // Pulse animation for icon
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(600)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = MinimalDarkColors.Indigo600.copy(alpha = 0.4f)
                )
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        MinimalDarkColors.Indigo500.copy(alpha = 0.6f + animatedOffset * 0.2f),
                        MinimalDarkColors.Purple500.copy(alpha = 0.6f + animatedOffset * 0.2f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MinimalDarkColors.Indigo600.copy(alpha = 0.3f),
                                MinimalDarkColors.Purple600.copy(alpha = 0.25f),
                                MinimalDarkColors.Indigo600.copy(alpha = 0.2f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header with animated icon
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated glowing icon
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(pulseScale)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MinimalDarkColors.Indigo400.copy(alpha = 0.4f),
                                            MinimalDarkColors.Indigo600.copy(alpha = 0.2f)
                                        )
                                    )
                                )
                                .border(
                                    2.dp,
                                    MinimalDarkColors.Indigo400.copy(alpha = 0.3f),
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MinimalDarkColors.Indigo400,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Smart Insights",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "AI-Powered Analysis",
                                fontSize = 12.sp,
                                color = MinimalDarkColors.Indigo200,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Expand indicator
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MinimalDarkColors.Gray400,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Divider with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        MinimalDarkColors.Indigo400.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Content
                    if (recommendation != null) {
                        ParsedInsights(
                            recommendation = recommendation,
                            isExpanded = isExpanded
                        )
                    } else {
                        NoInsightsPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
fun ParsedInsights(recommendation: String, isExpanded: Boolean) {
    // Try to parse the recommendation intelligently
    val lines = recommendation.split("\n").filter { it.isNotBlank() }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        lines.take(if (isExpanded) lines.size else 3).forEachIndexed { index, line ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(
                    animationSpec = tween(400, delayMillis = index * 100)
                ) + slideInVertically(
                    initialOffsetY = { 20 },
                    animationSpec = tween(400, delayMillis = index * 100)
                )
            ) {
                InsightLine(line, index)
            }
        }

        // Show "Read More" hint when collapsed
        if (!isExpanded && lines.size > 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap to read more...",
                    fontSize = 12.sp,
                    color = MinimalDarkColors.Indigo400,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MinimalDarkColors.Indigo400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Key metrics section (if expanded)
        if (isExpanded) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MinimalDarkColors.Purple400.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            QuickActionButtons()
        }
    }
}

@Composable
fun InsightLine(line: String, index: Int) {
    val isHeading = line.startsWith("###") || line.startsWith("**") && line.endsWith("**")
    val hasAmount = line.contains("₹") || line.contains("Rs")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = if (isHeading) Alignment.CenterVertically else Alignment.Top
    ) {
        // Bullet or icon
        if (!isHeading) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MinimalDarkColors.Indigo400)
            )
        }

        Text(
            text = line.replace("###", "")
                .replace("**", "")
                .trim(),
            fontSize = if (isHeading) 16.sp else 14.sp,
            lineHeight = 22.sp,
            fontWeight = if (isHeading) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isHeading -> Color.White
                hasAmount -> MinimalDarkColors.Indigo200
                else -> MinimalDarkColors.Gray300
            },
            modifier = Modifier.weight(1f)
        )

        // Highlight icon for important items
        if (line.contains("dominated") || line.contains("highest") || line.contains("large")) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = MinimalDarkColors.CategoryOrange,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun QuickActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            text = "Set Budget",
            icon = Icons.Default.AccountBalanceWallet,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            text = "Export",
            icon = Icons.Default.Download,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { /* TODO: Implement action */ },
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MinimalDarkColors.Indigo500.copy(alpha = 0.3f),
            contentColor = Color.White
        ),
        border = BorderStroke(1.dp, MinimalDarkColors.Indigo400.copy(alpha = 0.4f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun NoInsightsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Animated rotating icon
        val rotation by rememberInfiniteTransition(label = "rotation").animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .rotate(rotation)
                .clip(CircleShape)
                .background(MinimalDarkColors.Indigo600.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MinimalDarkColors.Indigo400,
                modifier = Modifier.size(28.dp)
            )
        }

        Text(
            text = "Analyzing your spending patterns...",
            fontSize = 14.sp,
            color = MinimalDarkColors.Gray300,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Add more expenses to get personalized insights",
            fontSize = 12.sp,
            color = MinimalDarkColors.Gray400,
            textAlign = TextAlign.Center
        )
    }
}

// Alternate version with category breakdown
@Composable
fun SmartInsightsCardWithCategories(
    recommendation: String?,
    topCategories: List<Pair<String, Double>> = emptyList()
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient offset"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(600)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = MinimalDarkColors.Indigo600.copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    colors = listOf(
                        MinimalDarkColors.Indigo500.copy(alpha = 0.6f + animatedOffset * 0.2f),
                        MinimalDarkColors.Purple500.copy(alpha = 0.6f + animatedOffset * 0.2f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MinimalDarkColors.Indigo600.copy(alpha = 0.3f),
                                MinimalDarkColors.Purple600.copy(alpha = 0.25f)
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MinimalDarkColors.Indigo500.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Insights,
                                null,
                                tint = MinimalDarkColors.Indigo400,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column {
                            Text(
                                "Smart Insights",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Financial Overview",
                                fontSize = 12.sp,
                                color = MinimalDarkColors.Indigo200
                            )
                        }
                    }

                    // Main insight
                    if (recommendation != null) {
                        Text(
                            recommendation,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MinimalDarkColors.Gray300
                        )
                    }

                    // Top categories
                    if (topCategories.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Top Spending Categories",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            topCategories.take(3).forEach { (category, amount) ->
                                CategoryInsightRow(category, amount)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryInsightRow(category: String, amount: Double) {
    val (color, icon) = getCategoryColorAndIcon(category)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MinimalDarkColors.Gray900.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = category,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "₹${String.format("%,.0f", amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111827)
@Composable
fun SmartInsightsCardPreview() {
    ExpenseTrackerMinimalDarkTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SmartInsightsCard(
                recommendation = """
                ### **1. Spending Patterns**
                
                Your total spending in this period was **₹166,913.56**. Your top three categories are:
                * **Bills:** ₹93,233.82 (driven by two large "CRED Club" payments)
                * **Shopping:** ₹50,996.23 (dominated by a single purchase from "Finterscale Technology")
                * **Other:** ₹15,555.25 (includes uncategorized payments)
                
                You also have frequent food delivery expenses from Swiggy, Dominos, and The Zaika King, totaling **₹3,949.68**.
                """.trimIndent()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111827)
@Composable
fun SmartInsightsCardWithCategoriesPreview() {
    ExpenseTrackerMinimalDarkTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SmartInsightsCardWithCategories(
                recommendation = "Your Bills category is significantly high this month, primarily due to credit card payments. Consider setting up alerts for large transactions.",
                topCategories = listOf(
                    "Bills" to 93233.82,
                    "Shopping" to 50996.23,
                    "Food" to 3949.68
                )
            )
        }
    }
}
