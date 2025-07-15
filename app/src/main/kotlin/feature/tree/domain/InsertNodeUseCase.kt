package com.example.composetree.feature.tree.domain

import com.example.composetree.core.model.ADDRESS_SIZE_BYTES
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.domain.InsertNodeUseCase.NodeNameProvider
import com.example.composetree.feature.tree.domain.NodeRespository.NodeRepositoryException
import kotlinx.io.bytestring.buildByteString
import javax.inject.Inject
import kotlin.random.Random

public fun interface InsertNodeUseCase {
    suspend fun insert(parent: EthereumAddress, name: EthereumAddress?): Result<Node>

    public fun interface NodeNameProvider {
        suspend fun getName(parent: EthereumAddress): EthereumAddress
    }
}

public class InsertNodeUseCaseImpl @Inject constructor(
    private val nodeRespository: NodeRespository,
    private val nodeNameProvider: NodeNameProvider = HashBasedNameProvider(),
) : InsertNodeUseCase {
    override suspend fun insert(parent: EthereumAddress, name: EthereumAddress?): Result<Node> {
        val newName = name ?: nodeNameProvider.getName(parent)
        val node = try {
            Node(newName, parent)
        } catch (iae: IllegalArgumentException) {
            return Result.failure(NodeRepositoryException("Invalid node address"))
        }
        return try {
            nodeRespository.insertNode(node)
            Result.success(node)
        } catch (exception: NodeRepositoryException) {
            Result.failure(exception)
        }
    }
}

public class HashBasedNameProvider(
    private val fallbackProvider: NodeNameProvider = RandomNameProvider()
) : NodeNameProvider {
    override suspend fun getName(parent: EthereumAddress): EthereumAddress {
        // Пытаемся как-нибудь применить хэш узла для формирования адреса, задание неразборчиво
        val node = Node(parent, ROOT_NODE_NAME)
        var hash: Int = System.identityHashCode(node)
        val bytes = buildByteString(ADDRESS_SIZE_BYTES) {
            repeat(16) {
                append(0)
            }
            repeat(4) {
                append((hash and 0xff).toByte())
                hash = hash ushr 8
            }
        }

        val name = EthereumAddress(bytes)
        return if (name != parent) {
            name
        } else {
            fallbackProvider.getName(parent)
        }
    }
}

public class RandomNameProvider(
    private val random: Random = Random.Default
) : NodeNameProvider {
    override suspend fun getName(parent: EthereumAddress): EthereumAddress {
        repeat(500) {
            val bytes = random.nextBytes(ADDRESS_SIZE_BYTES)
            val name = EthereumAddress(bytes)
            if (name != parent) {
                return name
            }
        }
        return ROOT_NODE_NAME
    }
}
