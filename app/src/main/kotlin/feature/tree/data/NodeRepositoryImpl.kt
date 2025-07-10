package com.example.composetree.feature.tree.data

import com.example.composetree.core.database.node.NodeDao
import com.example.composetree.core.database.node.NodeEntity
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.domain.NodeRespository
import com.example.composetree.feature.tree.domain.NodeRespository.NodeRepositoryException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Clock

public class NodeRepositoryImpl(
    private val nodeDao: NodeDao,
    private val computationDispatcherContext: CoroutineContext = Dispatchers.Main,
    private val clock: Clock = Clock.System,
) : NodeRespository {
    override suspend fun getNode(name: EthereumAddress): Node {
        if (name == ROOT_NODE_NAME) return ROOT_NODE
        return try {
            nodeDao.getNode(name).toNode()
        } catch (ex: RuntimeException) {
            coroutineContext.ensureActive()
            throw NodeRepositoryException(ex)
        }
    }

    override fun getChildNodes(name: EthereumAddress): Flow<List<EthereumAddress>> {
        val dbName = if (name != ROOT_NODE_NAME) name else null
        return nodeDao.getChildNodes(dbName)
            .flowOn(computationDispatcherContext)
            .map { list: List<NodeEntity> -> list.map(NodeEntity::name) }
    }

    override suspend fun insertNode(node: Node) {
        val nodeEntity = node.toNodeEntity(clock)
        return try {
            nodeDao.insertNode(nodeEntity)
        } catch (ex: RuntimeException) {
            coroutineContext.ensureActive()
            throw NodeRepositoryException(ex)
        }
    }

    override suspend fun deleteNodeRecursively(name: EthereumAddress) {
        return try {
            nodeDao.deleteNodeRecursively(name)
        } catch (ex: RuntimeException) {
            coroutineContext.ensureActive()
            throw NodeRepositoryException(ex)
        }
    }
}
