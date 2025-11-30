package com.anshul.expenseai.core.di

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeminiModule {

    @Provides
    @Singleton
    fun provideModelName(
        remoteConfig: FirebaseRemoteConfig
    ): String = remoteConfig.getString("model_name")

    @Provides
    @Singleton
    fun provideGeminiModel(
        modelName: String
    ): GenerativeModel {
        return Firebase.ai(backend = GenerativeBackend.vertexAI())
            .generativeModel("gemini-2.5-pro")
    }

}
