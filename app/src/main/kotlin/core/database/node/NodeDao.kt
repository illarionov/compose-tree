package com.example.composetree.core.database.node

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.composetree.core.model.Address
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao {
    @Query("SELECT * FROM node WHERE name = :name")
    suspend fun getNode(name: Address): NodeEntity

    @Query("SELECT * FROM node WHERE parent = :name ORDER BY created_at")
    fun getChildNodes(name: Address?): Flow<List<NodeEntity>>

    // TODO: remove?
    @Transaction
    @Query("SELECT * FROM node WHERE parent = :name ORDER BY created_at")
    fun getNodeWithChildren(name: Address?): Flow<List<NodeWithChildrenEntity>>

    @Insert
    suspend fun insertNode(nodeEntity: NodeEntity)

    @Query("DELETE from node WHERE name = :name")
    suspend fun deleteNodeRecursively(name: Address?)
}
