package com.example.composetree.wiring

import android.content.Context
import androidx.room.Room
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.composetree.core.database.AppDatabase
import com.example.composetree.core.database.node.NodeDao
import com.example.composetree.wiring.DispatcherModule.DatabaseCoroutineDispatcher
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    const val DATABASE_NAME = "db"

    @Provides
    fun providesNodeDao(database: AppDatabase): NodeDao = database.nodeDao()

    @Provides
    @Singleton
    fun providesDatabase(
        @ApplicationContext context: Context,
        @DatabaseCoroutineDispatcher queryCoroutineContext: CoroutineContext,
        sqliteDriver: SQLiteDriver
    ): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .setDriver(sqliteDriver)
            .setQueryCoroutineContext(queryCoroutineContext)
            .build()
    }

    @Provides
    @Reusable
    fun providesSqliteDriver(): SQLiteDriver = BundledSQLiteDriver()
}
