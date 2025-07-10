package com.example.composetree

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.logging.store.LoggingStoreFactory
import com.arkivanov.mvikotlin.timetravel.store.TimeTravelStoreFactory
import com.example.composetree.core.database.AppDatabase
import com.example.composetree.core.database.RealDatabaseFactory
import com.example.composetree.core.database.node.NodeDao
import com.example.composetree.feature.tree.data.NodeRepositoryImpl
import com.example.composetree.feature.tree.domain.NodeRespository

@SuppressLint("StaticFieldLeak")
private var applicationComponentInstance: ApplicationComponent? = null

val applicationComponent get() = checkNotNull(applicationComponentInstance) { "Application component not initialized" }

class ApplicationComponent private constructor(
    private val context: Context
) {
    val globalStoreFactoryInstance: StoreFactory = LoggingStoreFactory(delegate = TimeTravelStoreFactory())
    val database: AppDatabase = RealDatabaseFactory.create(context)

    val nodeDao: NodeDao get() = database.nodeDao()

    val nodeRepository: NodeRespository = NodeRepositoryImpl(nodeDao)

    companion object {

        internal fun initApplicationComponent(context: Context) {
            check(Looper.myLooper() == Looper.getMainLooper()) { "Should be called on main thread" }
            check(applicationComponentInstance == null)
            applicationComponentInstance = ApplicationComponent(context.applicationContext)
        }
    }
}
