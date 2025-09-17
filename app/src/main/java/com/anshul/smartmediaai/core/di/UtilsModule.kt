package com.anshul.smartmediaai.core.di

import android.content.Context
import android.content.SharedPreferences
import com.anshul.smartmediaai.BuildConfig
import com.anshul.smartmediaai.util.ExpenseDataFetcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilsModule {

    @Provides
    @Singleton
    fun provideExpenseDataFetcher(): ExpenseDataFetcher = ExpenseDataFetcher(BuildConfig.GOOGLE_API_KEY)

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
    }

}