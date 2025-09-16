package com.anshul.smartmediaai.core.di

import com.anshul.smartmediaai.BuildConfig
import com.anshul.smartmediaai.util.ExpenseDataFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideExpenseDataFetcher(): ExpenseDataFetcher = ExpenseDataFetcher(BuildConfig.GOOGLE_API_KEY)

}