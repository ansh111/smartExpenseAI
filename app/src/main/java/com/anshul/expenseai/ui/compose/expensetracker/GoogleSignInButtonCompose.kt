package com.anshul.expenseai.ui.compose.expensetracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun GoogleSignInButtonCompose(onButtonClick: () -> Unit) {
    AndroidView(factory = { context ->
        com.google.android.gms.common.SignInButton(context).apply {
            // Set up click listener or other properties
            setOnClickListener {
                onButtonClick.invoke()
            }
        }
    })
}
