package com.example.composetree.feature.tree

import com.example.composetree.core.model.Address
import com.example.composetree.core.model.Node

public data class NodeWithChildren(
    val node: Node,
    val children: Set<Address>
)

val NodeWithChildren.name: Address get() = node.name
val NodeWithChildren.parent: Address get() = node.parent
