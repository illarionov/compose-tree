package com.example.composetree.feature.tree.presentation

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.composetree.R
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.domain.DeleteNodeUseCase
import com.example.composetree.feature.tree.domain.InsertNodeUseCase
import com.example.composetree.feature.tree.domain.LoadNodeUseCase
import com.example.composetree.feature.tree.domain.LoadNodeUseCase.NodeWithChildFlow
import com.example.composetree.feature.tree.presentation.TreeScreenStore.SnackbarMessage
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.DeleteNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.NavigateBack
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.NavigateToNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.NavigateToRoot
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.ShowAddNodeDialog
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label.ScrollToNewNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.InitialLoad
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.MainContent
import com.example.composetree.feature.tree.presentation.TreeScreenStoreFactory.Msg.LoadNodeFailed
import com.example.composetree.feature.tree.presentation.TreeScreenStoreFactory.Msg.NodeContentUpdated
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal class TreeScreenStoreFactory(
    private val storeFactory: StoreFactory,
    private val loadNodeUseCase: LoadNodeUseCase,
    private val insertNodeUseCase: InsertNodeUseCase,
    private val deleteNodeUseCase: DeleteNodeUseCase,
) {
    fun create(
        nodeName: EthereumAddress,
    ): TreeScreenStore = object : TreeScreenStore,
        Store<Intent, TreeScreenState, Label> by storeFactory.create(
            name = "TreeScreenStore",
            initialState = InitialLoad(nodeName),
            bootstrapper = SimpleBootstrapper(Action.Init(nodeName)),
            reducer = ReducerImpl,
            executorFactory = { ExecutorImpl(loadNodeUseCase, insertNodeUseCase, deleteNodeUseCase) },
        ) {

    }

    private sealed interface Action {
        data class Init(val nodeName: EthereumAddress) : Action
    }

    private sealed interface Msg {
        data class NodeContentUpdated(val node: Node, val child: List<EthereumAddress>) : Msg
        data class LoadNodeFailed(
            val nodeName: EthereumAddress,
            val errorMessage: SnackbarMessage,
            val exception: Throwable,
        ) : Msg
    }

    private object ReducerImpl : Reducer<TreeScreenState, Msg> {
        override fun TreeScreenState.reduce(msg: Msg): TreeScreenState = when (this) {
            is InitialLoad -> when (msg) {
                is NodeContentUpdated -> MainContent(node = msg.node, child = msg.child)
                is LoadNodeFailed -> this
            }

            is MainContent -> when (msg) {
                is NodeContentUpdated -> copy(
                    node = msg.node,
                    child = msg.child,
                )

                is LoadNodeFailed -> this
            }
        }
    }

    private class ExecutorImpl(
        private val loadNodeUseCase: LoadNodeUseCase,
        private val insertNodeUseCase: InsertNodeUseCase,
        private val deleteNodeUseCase: DeleteNodeUseCase,
    ) : CoroutineExecutor<Intent, Action, TreeScreenState, Msg, Label>() {
        private var childNodesSubscription: Job = Job()

        override fun executeAction(action: Action) = when (action) {
            is Action.Init -> initialLoad(action.nodeName)
        }

        override fun executeIntent(intent: Intent) =
            when (intent) {
                is NavigateToNode -> navigateToNode(intent.name)
                NavigateBack -> navigateBack()
                NavigateToRoot -> navigateToNode(ROOT_NODE_NAME)
                is DeleteNode -> deleteNode(intent.name)
                ShowAddNodeDialog -> {
                    scope.launch {
                        val parent = state().nodeName
                        insertNodeUseCase
                            .insert(state().nodeName, null)
                            .onSuccess { newNode -> publish(ScrollToNewNode(newNode.name)) }
                            .onFailure { _: Throwable ->
                                val errorMessage = SnackbarMessage(
                                    R.string.failed_to_insert_node,
                                    listOf(parent.toEthereumString()),
                                )
                                publish(errorMessage)
                            }
                    }
                    Unit
                }
            }

        private fun initialLoad(nodeName: EthereumAddress) {
            scope.launch {
                loadNodeUseCase.load(nodeName)
                    .fold(
                        onSuccess = { node: NodeWithChildFlow ->
                            resubscribeToChildNodes(node.node, node.child)
                        },
                        onFailure = { exception: Throwable ->
                            dispatch(
                                LoadNodeFailed(
                                    nodeName = nodeName,
                                    errorMessage = SnackbarMessage(R.string.initial_load_failed),
                                    exception = exception,
                                ),
                            )
                        },
                    )
            }
        }

        private fun deleteNode(name: EthereumAddress) {
            val node = (state() as? MainContent)?.node
            scope.launch {
                deleteNodeUseCase.delete(name)
                    .fold(
                        onSuccess = {
                            if (name == node?.name) {
                                navigateToNode(node.name)
                            }
                            val statusMessage = SnackbarMessage(R.string.node_removed, listOf(name))
                            publish(statusMessage)
                        },
                        onFailure = { exception: Throwable ->
                            dispatch(
                                LoadNodeFailed(
                                    nodeName = name,
                                    errorMessage = SnackbarMessage(R.string.failed_to_delete_node),
                                    exception = exception,
                                ),
                            )
                        },
                    )
            }
        }

        private fun navigateBack() {
            val node = (state() as MainContent).node
            navigateToNode(node.parent)
        }

        private fun navigateToNode(name: EthereumAddress) {
            state().run {
                check(this is TreeScreenState.MainContent)
            }
            scope.launch {
                loadNodeUseCase.load(name)
                    .fold(
                        onSuccess = { node: NodeWithChildFlow ->
                            publish(Label.RootNodeChanged(name))
                            resubscribeToChildNodes(node.node, node.child)
                        },
                        onFailure = { _: Throwable ->
                            val errorMessage = SnackbarMessage(R.string.failed_to_load_child_nodes)
                            publish(errorMessage)
                        },
                    )
            }
        }

        private fun resubscribeToChildNodes(
            node: Node,
            child: Flow<List<EthereumAddress>>,
        ) {
            childNodesSubscription.cancel()
            childNodesSubscription = scope.launch {
                try {
                    child.collect { childNodes ->
                        dispatch(Msg.NodeContentUpdated(node, childNodes))
                    }
                } catch (collectException: Throwable) {
                    coroutineContext.ensureActive()
                    val errorMessage = SnackbarMessage(R.string.failed_to_load_child_nodes)
                    dispatch(Msg.LoadNodeFailed(node.name, errorMessage, collectException))
                    publish(errorMessage)
                }
            }
        }
    }
}
