package com.example.composetree.wiring

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import com.example.composetree.feature.tree.data.NodeRepositoryImpl
import com.example.composetree.feature.tree.domain.DeleteNodeUseCase
import com.example.composetree.feature.tree.domain.DeleteNodeUseCaseImpl
import com.example.composetree.feature.tree.domain.HashBasedNameProvider
import com.example.composetree.feature.tree.domain.InsertNodeUseCase
import com.example.composetree.feature.tree.domain.InsertNodeUseCase.NodeNameProvider
import com.example.composetree.feature.tree.domain.InsertNodeUseCaseImpl
import com.example.composetree.feature.tree.domain.LoadNodeUseCase
import com.example.composetree.feature.tree.domain.LoadNodeUseCaseImpl
import com.example.composetree.feature.tree.domain.NodeRespository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providesStoreFactory(): StoreFactory = LoggingStoreFactory(delegate = TimeTravelStoreFactory())

    @Provides
    fun providesClock(): Clock = Clock.System

    @Provides
    fun providesNodeNameProvider(): NodeNameProvider = HashBasedNameProvider()

    @Module
    @InstallIn(SingletonComponent::class)
    interface AppBindsModule {
        @Binds
        @Reusable
        fun bindsNodeRepository(impl: NodeRepositoryImpl): NodeRespository

        @Binds
        @Reusable
        fun bindsDeleteNodeUseCase(impl: DeleteNodeUseCaseImpl): DeleteNodeUseCase

        @Binds
        @Reusable
        fun bindsInsertNodeUseCase(impl: InsertNodeUseCaseImpl): InsertNodeUseCase

        @Binds
        @Reusable
        fun bindsLoadNodeUseCase(impl: LoadNodeUseCaseImpl): LoadNodeUseCase
    }
}
