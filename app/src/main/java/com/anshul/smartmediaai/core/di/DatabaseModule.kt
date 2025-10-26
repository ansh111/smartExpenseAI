package com.anshul.smartmediaai.core.di

import android.content.Context
import androidx.room.Room
import com.anshul.smartmediaai.data.db.ExpenseAnalyserDB
import com.anshul.smartmediaai.data.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): ExpenseAnalyserDB {
        return Room.databaseBuilder(
                context,
                ExpenseAnalyserDB::class.java,
                "expense_analyser_db"
            ).fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideExpenseDao(appDatabase: ExpenseAnalyserDB) = appDatabase.expenseDao()

}