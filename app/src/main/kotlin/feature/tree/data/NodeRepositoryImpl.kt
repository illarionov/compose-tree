package com.example.composetree.feature.tree.data

import android.database.SQLException
import com.example.composetree.core.database.node.NodeDao
import com.example.composetree.core.database.node.NodeEntity
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.domain.NodeRespository
import com.example.composetree.feature.tree.domain.NodeRespository.NodeRepositoryException
import com.example.composetree.feature.tree.domain.NodeRespository.RecordExistsException
import com.example.composetree.wiring.DispatcherModule.ComputationDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.time.Clock

class NodeRepositoryImpl @Inject constructor(
    private val nodeDao: NodeDao,
    @param:ComputationDispatcher
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
        if (node.name == ROOT_NODE_NAME) {
            // Root всегда существует, в базе для ссылки на него используется parent=null.
            throw RecordExistsException("Root node always exists")
        }
        val nodeEntity = node.toNodeEntity(clock)
        return try {
            nodeDao.insertNode(nodeEntity)
        } catch (sqlex: SQLException) {
            if (sqlex.isUniqueConstraintFailed()) {
                throw RecordExistsException("Node with name ${node.name} already exists", sqlex)
            } else {
                throw NodeRepositoryException("SQL error", sqlex)
            }
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
