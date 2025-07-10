package com.example.composetree.core.database.node

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.composetree.core.model.EthereumAddress

@Entity(
    tableName = "node",
    foreignKeys = [
        ForeignKey(
            entity = NodeEntity::class,
            parentColumns = ["name"],
            childColumns = ["parent"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ]
)
data class NodeEntity(
    @PrimaryKey val name: EthereumAddress,
    @ColumnInfo(index = true) val parent: EthereumAddress? = null,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: Long,
)
