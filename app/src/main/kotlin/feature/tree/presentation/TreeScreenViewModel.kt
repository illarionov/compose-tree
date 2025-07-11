package com.example.composetree.feature.tree.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.composetree.applicationComponent
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.domain.DeleteNodeUseCase
import com.example.composetree.feature.tree.domain.DeleteNodeUseCaseImpl
import com.example.composetree.feature.tree.domain.HashBasedNameProvider
import com.example.composetree.feature.tree.domain.InsertNodeUseCase
import com.example.composetree.feature.tree.domain.InsertNodeUseCase.NodeNameProvider
import com.example.composetree.feature.tree.domain.InsertNodeUseCaseImpl
import com.example.composetree.feature.tree.domain.LoadNodeUseCase
import com.example.composetree.feature.tree.domain.LoadNodeUseCaseImpl
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label.RootNodeChanged
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

const val CURRENT_NODE_KEY = "node"

internal class NodeTreeViewModel(
    storeFactoryInstance: StoreFactory,
    private val loadNodeUseCase: LoadNodeUseCase,
    private val insertNodeUseCase: InsertNodeUseCase,
    private val deleteNodeUseCase: DeleteNodeUseCase,
    private val nodeNameProvider: NodeNameProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val store: TreeScreenStore = run {
        TreeScreenStoreFactory(
            storeFactory = storeFactoryInstance,
            loadNodeUseCase = loadNodeUseCase,
            insertNodeUseCase = insertNodeUseCase,
            deleteNodeUseCase = deleteNodeUseCase,
            nodeNameProvider = nodeNameProvider,
        ).create(
            nodeName = savedStateHandle[CURRENT_NODE_KEY] ?: ROOT_NODE_NAME,
        )
    }

    val screenState: StateFlow<TreeScreenState> = store.stateFlow(viewModelScope)

    val labels: Flow<Label> get() = store.labels

    init {
        viewModelScope.launch {
            store.labels
                .filterIsInstance<RootNodeChanged>()
                .collect { rootNodeChanged -> savedStateHandle[CURRENT_NODE_KEY] = rootNodeChanged.name }
        }
    }

    fun acceptIntent(intent: Intent) = store.accept(intent)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }

    companion object {
        val Factory: Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val nodeRepository = applicationComponent.nodeRepository
                val loadNodeUseCase = LoadNodeUseCaseImpl(nodeRepository)
                val insertNodeUseCase = InsertNodeUseCaseImpl(nodeRepository)
                val deleteNodeUseCase = DeleteNodeUseCaseImpl(nodeRepository)
                val nodeNameProvider = HashBasedNameProvider()
                NodeTreeViewModel(
                    storeFactoryInstance = applicationComponent.globalStoreFactoryInstance,
                    savedStateHandle = savedStateHandle,
                    loadNodeUseCase = loadNodeUseCase,
                    insertNodeUseCase = insertNodeUseCase,
                    deleteNodeUseCase = deleteNodeUseCase,
                    nodeNameProvider = nodeNameProvider,
                )
            }
        }
    }
}
