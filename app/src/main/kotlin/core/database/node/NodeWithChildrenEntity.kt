package com.example.composetree.core.database.node

import androidx.room.Embedded
import androidx.room.Relation

data class NodeWithChildrenEntity(
    @Embedded val node: NodeEntity,
    @Relation(
        parentColumn = "name",
        entityColumn = "parent"
    ) val children: List<NodeEntity>,
)
