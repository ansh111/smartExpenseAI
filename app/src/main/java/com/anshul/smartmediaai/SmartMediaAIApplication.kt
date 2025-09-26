package com.anshul.smartmediaai

import android.app.Application
import android.webkit.WebView
import com.google.firebase.Firebase
import com.google.firebase.initialize
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SmartMediaAIApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(this)
        WebView.setWebContentsDebuggingEnabled(true)
    }
}