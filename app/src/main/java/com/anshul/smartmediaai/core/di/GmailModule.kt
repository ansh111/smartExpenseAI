package com.anshul.smartmediaai.core.di

import android.content.ContentResolver
import com.anshul.smartmediaai.data.repository.ExpenseRepo
import com.anshul.smartmediaai.data.repository.GmailRepo
import com.anshul.smartmediaai.data.repository.GmailRepoImpl
import com.anshul.smartmediaai.data.repository.ReadSmsRepo
import com.anshul.smartmediaai.data.repository.ReadSmsRepoImpl
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GmailModule {
    @Provides
    @Singleton
    fun provideGmailRepository(expenseRepo: ExpenseRepo, gson: Gson): GmailRepo {
        return GmailRepoImpl(expenseRepo, gson)
    }
}