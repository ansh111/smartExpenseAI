package com.anshul.expenseai.core.di

import com.anshul.expenseai.data.repository.ExpenseRepo
import com.anshul.expenseai.data.repository.GmailRepo
import com.anshul.expenseai.data.repository.GmailRepoImpl
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