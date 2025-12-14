package com.anshul.expenseai.ui.compose.expensetracker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anshul.expenseai.ui.theme.MinimalDarkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInBottomSheet(
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MinimalDarkColors.Gray900,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Title
            Text(
                text = "Get Expenses from Gmail",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MinimalDarkColors.Gray300,
                fontSize = 24.sp
            )

            // Description with card background
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MinimalDarkColors.Gray800.copy(alpha = 0.6f)
                ),
                border = BorderStroke(1.dp, MinimalDarkColors.Gray700.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Since SMS permission was not granted, you can connect your Google account to automatically fetch transaction emails from Gmail.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MinimalDarkColors.Gray400,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sign In Button
            GoogleSignInButtonCompose({
                onSignInClick()
            })

            // Optional: Dismiss button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Maybe later",
                    color = MinimalDarkColors.Gray500,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}
