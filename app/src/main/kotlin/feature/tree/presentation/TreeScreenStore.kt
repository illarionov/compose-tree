package com.example.composetree.feature.tree.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.arkivanov.mvikotlin.core.store.Store
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState

internal interface TreeScreenStore : Store<Intent, TreeScreenState, Label> {
    @Stable
    sealed interface TreeScreenState {
        val nodeName: EthereumAddress

        data class InitialLoad(override val nodeName: EthereumAddress = ROOT_NODE_NAME) : TreeScreenState

        data class MainContent(
            val node: Node,
            val child: List<EthereumAddress>,
            val insertNodeDialogState: InsertNodeDialogState? = null,
        ) : TreeScreenState {
            override val nodeName: EthereumAddress get() = node.name
        }

        data class InsertNodeDialogState(
            val textFieldState: TextFieldState = TextFieldState()
        )
    }

    @Immutable
    sealed class Intent  {
        data class NavigateToNode(val name: EthereumAddress) : Intent()
        data object NavigateToRoot: Intent()
        data object NavigateBack: Intent()
        data object ShowInsertNodeDialog : Intent()
        data object DismissInsertNodeDialog : Intent()
        data class ConfirmInsertNode(val parent: EthereumAddress, val name: String) : Intent()
        data class DeleteNode(val name: EthereumAddress) : Intent()
    }

    @Immutable
    sealed interface Label {
        data class RootNodeChanged(val name: EthereumAddress) : Label
        data class ScrollToNewNode(val name: EthereumAddress) : Label
    }

    @Immutable
    data class SnackbarMessage(
        @param:StringRes val resourceId: Int,
        val args: List<Any> = emptyList(),
    ) : Label
}

