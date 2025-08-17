package com.anshul.smartmediaai.ui.compose.videoai

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.anshul.smartmediaai.ui.compose.videoai.state.VideoSummarisationSideEffect
import com.anshul.smartmediaai.ui.compose.videoai.state.VideoSummarisationState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

class VideoSummarisationViewModel @Inject constructor() :
    ContainerHost<VideoSummarisationState, VideoSummarisationSideEffect>, ViewModel(){

    override val container = container<VideoSummarisationState, VideoSummarisationSideEffect>(
        VideoSummarisationState()
    )

    fun getSummary(videoSource: Uri) = intent {
        clearOutputText()
        val promptData =
            "Summarize this video in the form of top 3-4 takeaways only. Write in the form of bullet points. Don't assume if you don't know"
        reduce {
            state.copy(isLoading = true)
        }

        try {
            val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel("gemini-2.0-flash")
            val requestContent = content {
                fileData(videoSource.toString(), "video/mp4")
                text(promptData)
            }
            val outputSB = StringBuilder()
            generativeModel.generateContentStream(requestContent).collect{
                outputSB.append(it.text)
            }
            reduce {
                state.copy(
                    isLoading = false,
                    summary = outputSB.toString()
                )
            }
            postSideEffect(VideoSummarisationSideEffect.ShowToast("Summary generated"))
        } catch (e: Exception){
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
            postSideEffect(VideoSummarisationSideEffect.ShowToast(e.message ?: "Unknown error"))
        }


    }

    internal fun clearOutputText() = intent{
        reduce {
            state.copy(initial = "")
        }
    }




}
