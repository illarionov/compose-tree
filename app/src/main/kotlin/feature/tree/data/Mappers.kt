@file:OptIn(ExperimentalTime::class)

package com.example.composetree.feature.tree.data

import com.example.composetree.core.database.node.NodeEntity
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE_NAME
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun NodeEntity.toNode(): Node = Node(
    name = this.name,
    parent = this.parent ?: ROOT_NODE_NAME
)

@OptIn(ExperimentalTime::class)
fun Node.toNodeEntity(clock: Clock): NodeEntity {
    check(this.name != ROOT_NODE_NAME) { "We do not save the root node in the database" }
    return NodeEntity(
        name = this.name,
        parent = if (parent != ROOT_NODE_NAME) this.parent else null,
        createdAt = clock.now().epochSeconds,
    )
}
