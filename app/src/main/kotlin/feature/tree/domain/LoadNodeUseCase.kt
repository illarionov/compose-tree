package com.example.composetree.feature.tree.domain

import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.feature.tree.domain.LoadNodeUseCase.LoadNodeFailedException
import com.example.composetree.feature.tree.domain.LoadNodeUseCase.NodeWithChildFlow
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

public fun interface LoadNodeUseCase {
    suspend fun load(name: EthereumAddress): Result<NodeWithChildFlow>

    public data class NodeWithChildFlow(
        val node: Node,
        val child: Flow<List<EthereumAddress>>
    )

    public class LoadNodeFailedException(
        message: String,
        cause: Throwable,
    ) : RuntimeException(message, cause)
}

public class LoadNodeUseCaseImpl @Inject constructor(
    private val nodeRepository: NodeRespository,
) : LoadNodeUseCase {
    override suspend fun load(name: EthereumAddress): Result<NodeWithChildFlow>  {
        try {
            val node = nodeRepository.getNode(name)
            val child = nodeRepository.getChildNodes(name)
            return Result.success(NodeWithChildFlow(node, child))
        } catch (ex: RuntimeException) {
            coroutineContext.ensureActive()
            return Result.failure(LoadNodeFailedException("Failed to load node", ex))
        }
    }
}
