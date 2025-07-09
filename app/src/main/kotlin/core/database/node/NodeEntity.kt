package com.example.composetree.core.database.node

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.composetree.core.model.Address

// TODO: parent
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
    @PrimaryKey val name: Address,
    @ColumnInfo(index = true) val parent: Address? = null,
    @ColumnInfo(name = "created_at", defaultValue = "CURRENT_TIMESTAMP") val createdAt: Long, // TODO
)
