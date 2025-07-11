package com.example.composetree.feature.tree.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label.RootNodeChanged
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import javax.inject.Inject

const val CURRENT_NODE_KEY = "node"

@HiltViewModel
internal class NodeTreeViewModel @Inject constructor(
    treeScreenStoreFactory: TreeScreenStoreFactory,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val store: TreeScreenStore = treeScreenStoreFactory.create(
        nodeName = savedStateHandle[CURRENT_NODE_KEY] ?: ROOT_NODE_NAME,
    )

    val screenState: StateFlow<TreeScreenState> = store.stateFlow(viewModelScope)
    val labels: Flow<Label> get() = store.labels

    init {
        viewModelScope.launch {
            store.labels
                .filterIsInstance<RootNodeChanged>()
                .collect { rootNodeChanged ->
                    savedStateHandle[CURRENT_NODE_KEY] = rootNodeChanged.name
                }
        }
    }

    fun acceptIntent(intent: Intent) = store.accept(intent)

    override fun onCleared() {
        store.dispose()
        super.onCleared()
    }
}
