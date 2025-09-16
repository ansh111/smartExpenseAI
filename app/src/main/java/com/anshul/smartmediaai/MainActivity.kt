package com.anshul.smartmediaai

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerScreen
import com.anshul.smartmediaai.ui.compose.videoai.VideoSummarisationScreen
import com.anshul.smartmediaai.ui.theme.PrimaryBlue
import com.anshul.smartmediaai.ui.theme.SmartMediaAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartMediaAITheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = topAppBarColors(
                                containerColor = PrimaryBlue,
                                titleContentColor = Color.White,
                            ),
                            title = {
                                Text(text = stringResource(R.string.video_summarization_title))
                            }
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.background
                ){ innerPadding ->
                    ExpenseTrackerScreen(modifier = Modifier.padding(innerPadding))
                   // VideoSummarisationScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartMediaAITheme {
        Greeting("Android")
    }
}