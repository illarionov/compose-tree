package com.example.composetree.wiring

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @DatabaseCoroutineDispatcher
    fun providesDatabaseCoroutineDispatcher(): CoroutineContext = Dispatchers.IO

    @Provides
    @ComputationDispatcher
    fun providesComputationDispatcer(): CoroutineContext = Dispatchers.Default

    @Qualifier annotation class DatabaseCoroutineDispatcher
    @Qualifier annotation class ComputationDispatcher
}
