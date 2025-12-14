package com.anshul.expenseai.ui.compose.expensetracker.bottomsheet

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.anshul.expenseai.ui.theme.MinimalDarkColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInBottomSheetHost(
    activeSheet: GoogleSignInBottomSheet,
    onDismiss: () -> Unit,
    content: @Composable (GoogleSignInBottomSheet) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    LaunchedEffect(activeSheet) {
        if (activeSheet == GoogleSignInBottomSheet.None) {
            sheetState.hide()
        } else {
            sheetState.show()
        }
    }

    if (activeSheet != GoogleSignInBottomSheet.None) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss,
            containerColor = MinimalDarkColors.Gray900,
            scrimColor = Color.Black.copy(alpha = 0.6f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            content(activeSheet)
        }
    }
}
