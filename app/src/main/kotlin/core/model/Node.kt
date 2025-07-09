package com.example.composetree.core.model

/**
 * Адрес ROOT-ноды: 0
 */
public val ROOT_ADDRESS = Address(ByteArray(20))

/**
 * Адрес
 */
public val ROOT_NODE = Node(ROOT_ADDRESS, ROOT_ADDRESS)

/**
 * Нода в дереве без дочерних элементов
 */
public data class Node(
    val name: Address,
    val parent: Address
)

public val Node.isRoot: Boolean get() = this == ROOT_NODE
