package com.example.composetree.core.model

public data class NodeWithChildren(
    val node: Node,
    val child: Set<Address>
)

val NodeWithChildren.name: Address get() = node.name
val NodeWithChildren.parent: Address get() = node.parent
