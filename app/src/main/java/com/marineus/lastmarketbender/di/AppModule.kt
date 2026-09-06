package com.marineus.lastmarketbender.di

import android.content.Context
import android.content.SharedPreferences
import com.marineus.lastmarketbender.util.ConnectivityManagerNetworkMonitor
import com.marineus.lastmarketbender.util.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor
    ): NetworkMonitor

    companion object {
        @Provides
        @Singleton
        fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
            return context.getSharedPreferences("map_prefs", Context.MODE_PRIVATE)
        }
    }
}
