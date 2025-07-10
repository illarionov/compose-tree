package com.example.composetree.core.model

/**
 * Адрес ROOT-ноды: 0
 */
public val ROOT_NODE_NAME = EthereumAddress(ByteArray(20))

/**
 * Адрес
 */
public val ROOT_NODE = Node(ROOT_NODE_NAME, ROOT_NODE_NAME)

/**
 * Нода в дереве без дочерних элементов
 */
public data class Node(
    val name: EthereumAddress,
    val parent: EthereumAddress
) {
    init {
        if (name != ROOT_NODE_NAME) {
            require(name != parent)
        }
    }
}

public val Node.isRoot: Boolean get() = this == ROOT_NODE
