package com.anshul.expenseai.ui.compose.videoai.state

data class VideoSummarisationState (
    val isLoading: Boolean = false,
    val summary: String? = null,
    val errorMessage: String? = null,
    val initial: String? = null
)
