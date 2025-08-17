package com.anshul.smartmediaai.ui.compose.videoai.state

sealed class VideoSummarisationSideEffect {
    data class ShowToast(val message: String) : VideoSummarisationSideEffect()
    data class NavigateToDetails(val videoId: String) : VideoSummarisationSideEffect()
}
