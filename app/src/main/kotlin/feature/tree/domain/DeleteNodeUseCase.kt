package com.example.composetree.feature.tree.domain

import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.domain.DeleteNodeUseCase.DeleteNodeException
import com.example.composetree.feature.tree.domain.NodeRespository.NodeRepositoryException
import javax.inject.Inject

public fun interface DeleteNodeUseCase {
    suspend fun delete(name: EthereumAddress): Result<Unit>

    class DeleteNodeException : RuntimeException {
        constructor() : super()
        constructor(message: String?) : super(message)
        constructor(message: String?, cause: Throwable?) : super(message, cause)
        constructor(cause: Throwable?) : super(cause)
    }
}

public class DeleteNodeUseCaseImpl @Inject constructor(
    private val nodeRespository: NodeRespository,
) : DeleteNodeUseCase {
    override suspend fun delete(name: EthereumAddress): Result<Unit> {
        if (name == ROOT_NODE_NAME) {
            return Result.failure(DeleteNodeException("Can not remove root node"))
        }
        return try {
            nodeRespository.deleteNodeRecursively(name)
            Result.success(Unit)
        } catch (exception: NodeRepositoryException) {
            Result.failure(DeleteNodeException(exception))
        }
    }

}
