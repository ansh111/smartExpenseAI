package com.anshul.smartmediaai.ui.compose.expensetracker

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ExpenseChart(viewModel: ExpenseTrackerViewModel = hiltViewModel()){
    val state by viewModel.container.stateFlow.collectAsState()
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {

                settings.javaScriptEnabled = true // Essential for Chart.js
                settings.loadWithOverviewMode = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true // Optional: for zooming
                settings.displayZoomControls = false // Optional: hide zoom buttons
                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                webViewClient=object : WebViewClient() {
                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        Log.e("WebView", "Error loading URL: ${request?.url}")
                        super.onReceivedError(view, request, error)
                    }

                    override fun onLoadResource(view: WebView?, url: String?) {
                        Log.e("WebView", "Error loading URL from onLoadResource: ${view?.url}")
                        super.onLoadResource(view, url)
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        Log.d("WebView", "${consoleMessage.message()} [${consoleMessage.lineNumber()}]")
                        return true
                    }

                }
            }

        },
        update = { webView ->
            // The 'null' baseUrl is important for Chart.js to work correctly from a local HTML string.
            // The "text/html" mimeType tells WebView to render it as HTML.
            // "UTF-8" is a standard encoding.
            //add fata in form of chart if using chart.js
            webView.loadDataWithBaseURL("file:///", "", "text/html" ,"utf-8",null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Adjust height as needed
    )


}