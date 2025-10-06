package com.anshul.smartmediaai.core.di

import com.anshul.smartmediaai.core.network.ExpenseService
import com.anshul.smartmediaai.data.dao.ExpenseDao
import com.anshul.smartmediaai.data.repository.ExpenseLocalDataSource
import com.anshul.smartmediaai.data.repository.ExpenseRemoteDataSource
import com.anshul.smartmediaai.data.repository.ExpenseRepo
import com.anshul.smartmediaai.data.repository.ExpenseRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://dummy.com"

    @Provides
    @Singleton
    fun provideExpenseRepository(localDataSource: ExpenseLocalDataSource, remoteDataSource: ExpenseRemoteDataSource ): ExpenseRepo {
        return ExpenseRepoImpl(localDataSource, remoteDataSource)

    }

    @Provides
    @Singleton
    fun provideExpenseDataSource(expenseDao: ExpenseDao): ExpenseLocalDataSource {
        return ExpenseLocalDataSource(expenseDao)
    }

    @Provides
    @Singleton
    fun provideExpenseRemoteDataSource(service: ExpenseService): ExpenseRemoteDataSource {
        return ExpenseRemoteDataSource(service)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit{
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideExpenseService(retrofit: Retrofit): ExpenseService {
         return  retrofit.create(ExpenseService::class.java)
    }





}