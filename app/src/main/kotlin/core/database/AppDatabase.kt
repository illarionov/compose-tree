package com.example.composetree.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.composetree.core.database.converter.AddressConverter
import com.example.composetree.core.database.node.NodeDao
import com.example.composetree.core.database.node.NodeEntity

@Database(
    version = 1,
    entities = [
        NodeEntity::class,
    ],
)
@TypeConverters(AddressConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
}
