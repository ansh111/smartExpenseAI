package com.anshul.expenseai.core.di

import android.content.ContentResolver
import android.content.Context
import com.anshul.expenseai.data.repository.ReadSmsRepo
import com.anshul.expenseai.data.repository.ReadSmsRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SMSModule {

    @Provides
    @Singleton
    fun provideSMSRepository(contentResolver: ContentResolver): ReadSmsRepo {
        return ReadSmsRepoImpl(contentResolver = contentResolver)
    }


    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }



}