package com.anshul.expenseai.core.di

import android.util.Log
import com.anshul.expenseai.R
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig {
        val remoteConfig = Firebase.remoteConfig

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        // Fetch and activate once during initialization (non-blocking)
        remoteConfig.fetchAndActivate()
            .addOnSuccessListener {
                Log.d("RemoteConfig", "Remote config fetched")
            }
            .addOnFailureListener { e ->
                Log.e("RemoteConfig", "Remote config failed to fetch", e)
            }

        return remoteConfig
    }
}
