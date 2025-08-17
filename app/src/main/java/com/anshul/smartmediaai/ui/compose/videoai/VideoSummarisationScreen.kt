package com.anshul.smartmediaai.ui.compose.videoai

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.ExoPlayer
import com.anshul.smartmediaai.R
import com.anshul.smartmediaai.util.sampleVideoList
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun VideoSummarisationScreen(
    viewModel:  VideoSummarisationViewModel = hiltViewModel(),
    modifier: Modifier
){

    var selectedVideoUri by remember { mutableStateOf<Uri?>(sampleVideoList.first().uri) }
    val context = LocalContext.current
    val videoSummarisationState by viewModel.collectAsState()
    var youtubePlayer: YouTubePlayer? by remember { mutableStateOf(null) }
    var currentYouTubeVideoId: String? by remember { mutableStateOf(null) }
    var showYoutubeLoading by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val videoOptions = sampleVideoList
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }

    }

    LaunchedEffect(selectedVideoUri) {
        selectedVideoUri?.takeIf { it.toString().isNotEmpty()  }?.let { uri ->
            val videoUrl = uri.toString()
            if(videoUrl.contains("youtu.be") || videoUrl.contains("youtube.com")){
                val videoId = extractYouTubeVideoId(videoUrl)
                if (videoId != null) {
                    currentYouTubeVideoId = videoId
                    youtubePlayer?.loadVideo(videoId, 0f)
                } else {
                    Log.e("VideoPlayer", "Could not extract YouTube video ID from URL: $videoUrl")
                }
                exoPlayer.stop()

            }

        }
    }

    Box(modifier = modifier){
        Column(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)) {

            Spacer(modifier = Modifier.height(16.dp))

            VideoSelectionDropdown(
                selectedVideoUri = selectedVideoUri,
                isDropdownExpanded = isDropdownExpanded,
                videoOptions = videoOptions,
                onVideoUriSelected = { uri ->
                    selectedVideoUri = uri
                    viewModel.clearOutputText()
                },
                onDropdownExpanded = { expanded ->
                    isDropdownExpanded = expanded
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (currentYouTubeVideoId != null){
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            YouTubePlayerView(context).apply {
                                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        youtubePlayer = youTubePlayer
                                        currentYouTubeVideoId?.let { youTubePlayer.loadVideo(it, 0f) }
                                    }

                                    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                                        showYoutubeLoading = state == PlayerConstants.PlayerState.BUFFERING
                                    }
                                })
                            }
                        }
                    )
                    if (showYoutubeLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            } else {
                VideoPlayer(exoPlayer = exoPlayer, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.getSummary(selectedVideoUri!!)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(R.string.summarize_video_button), color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if( videoSummarisationState.isLoading){
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center

                ) {
                    CircularProgressIndicator()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = videoSummarisationState.summary ?: "",
                style = MaterialTheme.typography.bodyMedium
            )



        }
    }


}

fun extractYouTubeVideoId(youtubeUrl: String): String? {
    val youtubeRegex = "(?:https?:\\/\\/(?:www\\.)?|www\\.)(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?.+&v=))([\\w-]{11})(?:\\S+)?".toRegex()
    val matchResult = youtubeRegex.find(youtubeUrl)
    return matchResult?.groups?.get(1)?.value
}