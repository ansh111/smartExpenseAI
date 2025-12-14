package com.anshul.expenseai.ui.compose.expensetracker

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anshul.expenseai.ui.theme.MinimalDarkColors

@Composable
fun HeaderLoadingShimmer() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    // Shimmer animation
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer translate"
    )
    
    // Pulse animation
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column {
        // "Total Expenses" label shimmer
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.25f),
                            Color.White.copy(alpha = 0.15f)
                        ),
                        start = Offset(shimmerTranslate * 800f - 400f, 0f),
                        end = Offset(shimmerTranslate * 800f + 400f, 0f)
                    )
                )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Amount shimmer (larger)
        Box(
            modifier = Modifier
                .width(180.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.2f)
                        ),
                        start = Offset(shimmerTranslate * 1000f - 500f, 0f),
                        end = Offset(shimmerTranslate * 1000f + 500f, 0f)
                    )
                )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Subtitle shimmer
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.12f)
                        ),
                        start = Offset(shimmerTranslate * 800f - 400f, 0f),
                        end = Offset(shimmerTranslate * 800f + 400f, 0f)
                    )
                )
        )
    }
}

// Alternative: Animated dots loading
@Composable
fun HeaderLoadingAnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Expenses",
            color = MinimalDarkColors.Indigo200,
            fontSize = 13.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Loading",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
            
            repeat(3) { index ->
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            delayMillis = index * 150,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot $index"
                )
                
                Box(
                    modifier = Modifier
                        .size((8 * scale).dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Calculating total...",
            color = MinimalDarkColors.Indigo200,
            fontSize = 12.sp
        )
    }
}

// Alternative: Pulse effect
@Composable
fun HeaderLoadingPulse() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Total Expenses",
            color = MinimalDarkColors.Indigo200.copy(alpha = alpha),
            fontSize = 13.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .size((56 * scale).dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = alpha * 0.4f),
                            Color.White.copy(alpha = alpha * 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color.White.copy(alpha = alpha),
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Loading data...",
            color = MinimalDarkColors.Indigo200.copy(alpha = alpha),
            fontSize = 12.sp
        )
    }
}

// Alternative: Skeleton with breathing animation
@Composable
fun HeaderLoadingBreathing() {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Label
        Box(
            modifier = Modifier
                .width(110.dp)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color.White.copy(alpha = 0.15f))
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Main amount with shimmer and breathing
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.35f * breatheScale),
                            Color.White.copy(alpha = 0.2f)
                        ),
                        start = Offset(shimmerTranslate * 1200f - 600f, 0f),
                        end = Offset(shimmerTranslate * 1200f + 600f, 0f)
                    )
                )
        )
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Subtitle
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            )
            
            // Animated dots
            repeat(3) { index ->
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 600,
                            delayMillis = index * 200,
                            easing = FastOutSlowInEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot $index"
                )
                
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White.copy(alpha = alpha))
                )
            }
        }
    }
}

// Preview
@Preview(showBackground = true, backgroundColor = 0xFF4F46E5)
@Composable
fun HeaderLoadingShimmerPreview() {
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
        HeaderLoadingShimmer()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4F46E5)
@Composable
fun HeaderLoadingAnimatedDotsPreview() {
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
        HeaderLoadingAnimatedDots()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4F46E5)
@Composable
fun HeaderLoadingPulsePreview() {
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
        HeaderLoadingPulse()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF4F46E5)
@Composable
fun HeaderLoadingBreathingPreview() {
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
        HeaderLoadingBreathing()
    }
}
