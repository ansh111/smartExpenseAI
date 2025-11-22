package com.anshul.expenseai.ui.compose.expensetracker

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.TableInfo.Column
import com.anshul.expenseai.ui.theme.MinimalDarkColors
import androidx.compose.runtime.getValue

@Composable
fun SmartInsightsLoadingCardDetailed() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = MinimalDarkColors.Indigo600.copy(alpha = 0.4f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            1.5.dp,
            Brush.linearGradient(
                colors = listOf(
                    MinimalDarkColors.Indigo500.copy(alpha = borderPulse),
                    MinimalDarkColors.Purple500.copy(alpha = borderPulse)
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
                    // Icon placeholder with pulse
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MinimalDarkColors.Indigo500.copy(alpha = 0.3f + shimmerTranslate * 0.2f),
                                        MinimalDarkColors.Purple500.copy(alpha = 0.3f + shimmerTranslate * 0.2f)
                                    )
                                )
                            )
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedShimmerBox(
                            width = 150.dp,
                            height = 24.dp,
                            progress = shimmerTranslate
                        )
                        AnimatedShimmerBox(
                            width = 110.dp,
                            height = 14.dp,
                            progress = shimmerTranslate
                        )
                    }
                }
                
                // Divider
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
                
                // Content lines
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    repeat(4) { index ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Bullet
                            Box(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MinimalDarkColors.Indigo400.copy(alpha = 0.5f))
                            )
                            
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AnimatedShimmerBox(
                                    width = if (index % 2 == 0) 260.dp else 230.dp,
                                    height = 14.dp,
                                    progress = shimmerTranslate
                                )
                                if (index < 2) {
                                    AnimatedShimmerBox(
                                        width = 160.dp,
                                        height = 14.dp,
                                        progress = shimmerTranslate
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Status text
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AI is analyzing your expenses",
                            fontSize = 13.sp,
                            color = MinimalDarkColors.Indigo300
                        )
                        LoadingDotsSimple()
                    }
                }
            }
        }
    }
}


@Composable
fun AnimatedShimmerBox(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    progress: Float
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MinimalDarkColors.Gray700.copy(alpha = 0.3f),
                        MinimalDarkColors.Indigo400.copy(alpha = 0.3f),
                        MinimalDarkColors.Purple400.copy(alpha = 0.3f),
                        MinimalDarkColors.Gray700.copy(alpha = 0.3f)
                    ),
                    start = Offset(progress * 1500f - 500f, 0f),
                    end = Offset(progress * 1500f + 500f, 0f)
                )
            )
    )
}

@Composable
fun LoadingDotsSimple() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 600,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )
            
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(MinimalDarkColors.Indigo300.copy(alpha = alpha))
            )
        }
    }
}