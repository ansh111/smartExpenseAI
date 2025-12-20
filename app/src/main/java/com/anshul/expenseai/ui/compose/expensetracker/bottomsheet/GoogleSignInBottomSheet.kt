package com.anshul.expenseai.ui.compose.expensetracker.bottomsheet

sealed interface GoogleSignInBottomSheet {
    data object None : GoogleSignInBottomSheet
    data object GoogleSignIn : GoogleSignInBottomSheet
    data object GoogleSignInUsingCred: GoogleSignInBottomSheet
}