package com.marineus.lastmarketbender.di

import android.content.Context
import androidx.room.Room
import com.marineus.lastmarketbender.data.local.AppDatabase
import com.marineus.lastmarketbender.data.local.PinDao
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "market_database"
        )
            .fallbackToDestructiveMigration(false)
        .build()
    }

    @Provides
    fun providePinDao(database: AppDatabase): PinDao {
        return database.pinDao()
    }
}
