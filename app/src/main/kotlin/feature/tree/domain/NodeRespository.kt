package com.example.composetree.feature.tree.domain

import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import kotlinx.coroutines.flow.Flow

public interface NodeRespository {
    suspend fun insertNode(node: Node)

    suspend fun getNode(name: EthereumAddress): Node

    fun getChildNodes(name: EthereumAddress): Flow<List<EthereumAddress>>

    suspend fun deleteNodeRecursively(name: EthereumAddress)

    public open class NodeRepositoryException : RuntimeException {
        constructor() : super()
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
        constructor(cause: Throwable?) : super(cause)
    }

    public class RecordExistsException(
        message: String?,
        cause: Throwable? = null,
    ) : NodeRepositoryException(message, cause)
}
