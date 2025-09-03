package com.anshul.smartmediaai.core.di

import com.anshul.smartmediaai.data.dao.ExpenseDao
import com.anshul.smartmediaai.data.repository.ExpenseDataSource
import com.anshul.smartmediaai.data.repository.ExpenseRepo
import com.anshul.smartmediaai.data.repository.ExpenseRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideExpenseRepository(localDataSource: ExpenseDataSource): ExpenseRepo {
        return ExpenseRepoImpl(localDataSource)

    }

    @Provides
    @Singleton
    fun provideExpenseDataSource(expenseDao: ExpenseDao): ExpenseDataSource {
        return ExpenseDataSource(expenseDao)
    }



}