package com.example.composetree.core.database.node

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.composetree.core.model.EthereumAddress
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao {
    @Query("SELECT * FROM node WHERE name = :name")
    suspend fun getNode(name: EthereumAddress): NodeEntity

    @Query("SELECT * FROM node WHERE parent IS :name ORDER BY created_at DESC")
    fun getChildNodes(name: EthereumAddress?): Flow<List<NodeEntity>>

    @Insert
    suspend fun insertNode(nodeEntity: NodeEntity)

    @Query("DELETE from node WHERE name = :name")
    suspend fun deleteNodeRecursively(name: EthereumAddress?)
}
