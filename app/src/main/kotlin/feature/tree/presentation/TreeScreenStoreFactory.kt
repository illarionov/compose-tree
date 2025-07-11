package com.example.composetree.feature.tree.presentation

import androidx.compose.foundation.text.input.TextFieldState
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.example.composetree.R
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.core.model.toEthereumAddress
import com.example.composetree.feature.tree.domain.DeleteNodeUseCase
import com.example.composetree.feature.tree.domain.InsertNodeUseCase
import com.example.composetree.feature.tree.domain.InsertNodeUseCase.NodeNameProvider
import com.example.composetree.feature.tree.domain.LoadNodeUseCase
import com.example.composetree.feature.tree.domain.LoadNodeUseCase.NodeWithChildFlow
import com.example.composetree.feature.tree.domain.NodeRespository.RecordExistsException
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.ConfirmInsertNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.DeleteNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.NavigateBack
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.NavigateToNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.NavigateToRoot
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.ShowInsertNodeDialog
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label.ScrollToNewNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.SnackbarMessage
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.InitialLoad
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.InsertNodeDialogState
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.MainContent
import com.example.composetree.feature.tree.presentation.TreeScreenStoreFactory.Action.InsertNode
import com.example.composetree.feature.tree.presentation.TreeScreenStoreFactory.Msg.LoadNodeFailed
import com.example.composetree.feature.tree.presentation.TreeScreenStoreFactory.Msg.NodeContentUpdated
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@ViewModelScoped
internal class TreeScreenStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val loadNodeUseCase: LoadNodeUseCase,
    private val insertNodeUseCase: InsertNodeUseCase,
    private val deleteNodeUseCase: DeleteNodeUseCase,
    private val nodeNameProvider: NodeNameProvider,
) {
    fun create(
        nodeName: EthereumAddress,
    ): TreeScreenStore = object : TreeScreenStore,
        Store<Intent, TreeScreenState, Label> by storeFactory.create(
            name = "TreeScreenStore",
            initialState = InitialLoad(nodeName),
            bootstrapper = SimpleBootstrapper(Action.Init(nodeName)),
            reducer = ReducerImpl,
            executorFactory = {
                ExecutorImpl(
                    loadNodeUseCase,
                    insertNodeUseCase,
                    deleteNodeUseCase,
                    nodeNameProvider
                )
            }
        ) {
    }

    private sealed interface Action {
        data class Init(val nodeName: EthereumAddress) : Action
        data class InsertNode(val parent: EthereumAddress, val name: EthereumAddress) : Action
    }

    private sealed interface Msg {
        data class NodeContentUpdated(val node: Node, val child: List<EthereumAddress>) : Msg
        data class LoadNodeFailed(
            val nodeName: EthereumAddress,
            val errorMessage: SnackbarMessage,
            val exception: Throwable,
        ) : Msg

        data class ShowInsertNodeDialog(val newName: EthereumAddress) : Msg
        data object DismissInsertNodeDialog : Msg
    }

    private object ReducerImpl : Reducer<TreeScreenState, Msg> {
        override fun TreeScreenState.reduce(msg: Msg): TreeScreenState = when (this) {
            is InitialLoad -> when (msg) {
                is NodeContentUpdated -> MainContent(node = msg.node, child = msg.child)
                is LoadNodeFailed -> this
                else -> error("Unexpected message $msg")
            }

            is MainContent -> when (msg) {
                is NodeContentUpdated -> copy(
                    node = msg.node,
                    child = msg.child,
                )

                is LoadNodeFailed -> this
                is Msg.ShowInsertNodeDialog -> copy(
                    insertNodeDialogState = InsertNodeDialogState(
                        textFieldState = TextFieldState(initialText = msg.newName.toEthereumString()),
                    ),
                )

                Msg.DismissInsertNodeDialog -> copy(insertNodeDialogState = null)
            }
        }
    }

    private class ExecutorImpl(
        private val loadNodeUseCase: LoadNodeUseCase,
        private val insertNodeUseCase: InsertNodeUseCase,
        private val deleteNodeUseCase: DeleteNodeUseCase,
        private val nodeNameProvider: NodeNameProvider,
    ) : CoroutineExecutor<Intent, Action, TreeScreenState, Msg, Label>() {
        private var childNodesSubscription: Job = Job()

        override fun executeAction(action: Action) = when (action) {
            is Action.Init -> initialLoad(action.nodeName)
            is InsertNode -> insertNode(action.parent, action.name)
        }

        override fun executeIntent(intent: Intent) =
            when (intent) {
                is NavigateToNode -> navigateToNode(intent.name)
                NavigateBack -> navigateBack()
                NavigateToRoot -> navigateToNode(ROOT_NODE_NAME)

                is DeleteNode -> deleteNode(intent.name)
                ShowInsertNodeDialog -> {
                    scope.launch {
                        val suggestedName = nodeNameProvider.getName(state().nodeName)
                        dispatch(Msg.ShowInsertNodeDialog(suggestedName))
                    }
                    Unit
                }

                Intent.DismissInsertNodeDialog -> dispatch(Msg.DismissInsertNodeDialog)
                is ConfirmInsertNode -> confirmInsertNode(intent.parent, intent.name)
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
                                    errorMessage = SnackbarMessage(R.string.snackbar_msg_initial_load_failed),
                                    exception = exception,
                                ),
                            )
                        },
                    )
            }
        }

        private fun confirmInsertNode(
            parent: EthereumAddress,
            name: String,
        ) {
            val newAddress = try {
                name.toEthereumAddress()
            } catch (iae: IllegalArgumentException) {
                val errorMessage = SnackbarMessage(R.string.snackbar_msg_node_name_is_not_valid)
                publish(errorMessage)
                return
            }

            dispatch(Msg.DismissInsertNodeDialog)
            forward(Action.InsertNode(parent, newAddress))
        }

        private fun insertNode(
            parent: EthereumAddress,
            name: EthereumAddress? = null,
        ) {
            scope.launch {
                insertNodeUseCase
                    .insert(parent, name)
                    .onSuccess { newNode -> publish(ScrollToNewNode(newNode.name)) }
                    .onFailure { ex: Throwable ->
                        val errorMessage = when {
                            ex is RecordExistsException -> SnackbarMessage(
                                R.string.snackbar_msg_node_exists,
                                listOf(parent.toEthereumString()),
                            )

                            else -> SnackbarMessage(
                                R.string.snackbar_msg_failed_to_insert_node,
                                listOf(parent.toEthereumString()),
                            )
                        }
                        publish(errorMessage)
                    }
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
                            val statusMessage =
                                SnackbarMessage(R.string.snackbar_msg_node_removed, listOf(name))
                            publish(statusMessage)
                        },
                        onFailure = { exception: Throwable ->
                            dispatch(
                                LoadNodeFailed(
                                    nodeName = name,
                                    errorMessage = SnackbarMessage(R.string.snackbar_msg_failed_to_delete_node),
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
