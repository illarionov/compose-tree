@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalMaterial3Api::class,
)

package com.example.composetree.feature.tree.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.composetree.R
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.Node
import com.example.composetree.core.model.ROOT_NODE
import com.example.composetree.core.model.isRoot
import com.example.composetree.core.model.toEthereumAddress
import com.example.composetree.core.ui.theme.ComposeTreeTheme
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.DeleteNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent.NavigateToNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Label.ScrollToNewNode
import com.example.composetree.feature.tree.presentation.TreeScreenStore.SnackbarMessage
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.InitialLoad
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.MainContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance

@Composable
internal fun NodeTreeRoot(
    state: TreeScreenState,
    labels: Flow<Label>,
    onIntent: (Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources
    LaunchedEffect(resources) {
        labels
            .filterIsInstance<SnackbarMessage>()
            .collect { error -> snackbarHostState.showSnackbar(resources.getString(error)) }
    }

    BackHandler(
        enabled = state is MainContent && !state.node.isRoot,
    ) {
        onIntent(Intent.NavigateBack)
    }

    SharedTransitionLayout(
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = state,
            label = "basic_transition",
            contentKey = { state -> state.nodeName },
            transitionSpec = {
                fadeIn(
                    animationSpec = MotionScheme.expressive().slowEffectsSpec(),
                ).togetherWith(
                    fadeOut(
                        animationSpec = MotionScheme.expressive().slowEffectsSpec(),
                    ),
                ).using(
                    SizeTransform(
                        clip = false,
                        sizeAnimationSpec = { initialSize, targetSize ->
                            MotionScheme.expressive().slowSpatialSpec()
                        },
                    ),
                )
            },
        ) { state ->
            NodeTreeScaffold(
                state = state,
                snackbarHostState = snackbarHostState,
                labels = labels,
                onIntent = onIntent,
                animatedVisibilityScope = this@AnimatedContent,
                sharedTransitionScope = this@SharedTransitionLayout,
            )
        }
    }
}

@Composable
internal fun NodeTreeScaffold(
    state: TreeScreenState,
    snackbarHostState: SnackbarHostState,
    labels: Flow<Label>,
    onIntent: (Intent) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.nodeName.localizedText(),
                        maxLines = 1,
                        overflow = TextOverflow.MiddleEllipsis,
                    )
                },
                navigationIcon = {
                    if (state is MainContent && !state.node.isRoot) {
                        IconButton(onClick = { onIntent(Intent.NavigateToRoot) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.navigate_up),
                            )
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (state is TreeScreenState.MainContent) {
                FloatingActionButton(
                    content = {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_node))
                    },
                    onClick = { onIntent(Intent.ShowInsertNodeDialog) },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding: PaddingValues ->
        when (state) {
            is InitialLoad -> {
                /* Placeholder */
            }

            is MainContent -> NodeTreeMainContent(
                state = state,
                onIntent = onIntent,
                labels = labels,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
internal fun NodeTreeMainContent(
    state: TreeScreenState.MainContent,
    labels: Flow<Label>,
    onIntent: (Intent) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) {
    NodeTreeNodesContent(
        modifier = modifier,
        node = state.node,
        child = state.child,
        labels = labels,
        onIntent = onIntent,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
    )
    if (state.insertNodeDialogState != null) {
        InsertNodeDialog(
            parentNodeName = state.nodeName,
            state = state.insertNodeDialogState,
            onIntent = onIntent,
        )
    }
}

@Composable
internal fun NodeTreeNodesContent(
    node: Node,
    child: List<EthereumAddress>,
    labels: Flow<Label>,
    onIntent: (Intent) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
) = with(sharedTransitionScope) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(horizontalInsets()),
        ) {
            Text(
                text = stringResource(R.string.node_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = R.string.node_title),
                    animatedVisibilityScope = animatedVisibilityScope,
                ),
            )
            SelectionContainer {
                Text(
                    text = node.name.toEthereumString(),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .sharedBounds(
                            rememberSharedContentState(key = node.name),
                            animatedVisibilityScope = animatedVisibilityScope,
                            zIndexInOverlay = 10f,
                        )
                        .padding(top = 8.dp),
                )
            }
            Text(
                text = stringResource(R.string.child_nodes),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        if (child.isEmpty()) {
            Text(
                text = stringResource(R.string.child_node_list_is_empty),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(horizontalInsets())
                    .padding(top = 8.dp),
                textAlign = TextAlign.Start,
            )
        } else {
            ChildNodes(
                nodes = child,
                scrollLabels = labels.filterIsInstance<ScrollToNewNode>(),
                onIntent = onIntent,
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
            )
        }
    }
}

@Composable
private fun ChildNodes(
    nodes: List<EthereumAddress>,
    scrollLabels: Flow<ScrollToNewNode>,
    onIntent: (Intent) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val lazyListState = rememberLazyListState()

    LaunchedEffect(lazyListState) {
        scrollLabels
            .collect {
                delay(200)
                lazyListState.animateScrollToItem(0)
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 8.dp),
        contentPadding = WindowInsets.safeContent
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues(),
        verticalArrangement = spacedBy(4.dp),
        state = lazyListState,
    ) {
        items(
            count = nodes.size,
            key = { nodes[it] },
        ) { itemIndex ->
            val childName: EthereumAddress = nodes[itemIndex]
            ChildNodeListItem(
                name = childName,
                enabled = enabled,
                onClick = { onIntent(NavigateToNode(it)) },
                onRemove = { onIntent(DeleteNode(it)) },
                animatedVisibilityScope = animatedVisibilityScope,
                sharedTransitionScope = sharedTransitionScope,
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
            )
        }
    }
}

@Composable
fun ChildNodeListItem(
    name: EthereumAddress,
    enabled: Boolean,
    onClick: (EthereumAddress) -> Unit,
    onRemove: (EthereumAddress) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = modifier.fillMaxSize(),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                EndToStart -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.remove_node),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red)
                            .wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = Color.White,
                    )
                }

                else -> {}
            }
        },
        onDismiss = { direction ->
            if (direction == EndToStart) {
                onRemove(name)
            } else {
                swipeToDismissBoxState.reset()
            }
        },
    ) {
        OutlinedCard(
            modifier = Modifier
                .clickable(enabled = enabled, onClick = { onClick(name) })
                .windowInsetsPadding(horizontalInsets()),
        ) {
            ListItem(
                headlineContent = {
                    with(sharedTransitionScope) {
                        Text(
                            name.toEthereumString(),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.sharedBounds(
                                rememberSharedContentState(key = name),
                                animatedVisibilityScope = animatedVisibilityScope,
                                zIndexInOverlay = 10f,
                            ),
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun horizontalInsets(): WindowInsets = WindowInsets.systemGestures
    .only(WindowInsetsSides.Horizontal)
    .union(WindowInsets(left = 16.dp, right = 16.dp))


@Preview(showBackground = true)
@Composable
fun NodeTreeScreenPreviewMainContent() {
    ComposeTreeTheme {
        NodeTreeRoot(
            state = MainContent(
                node = ROOT_NODE,
                child = listOf(
                    "0x000102030405060708090a0b0c0d0e0f10111213".toEthereumAddress(),
                    "0x100102030405060708090a0b0c0d0e0f10111213".toEthereumAddress(),
                ),
            ),
            labels = emptyFlow(),
            onIntent = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NodeTreeScreenPreviewPlaceholder() {
    ComposeTreeTheme {
        NodeTreeRoot(
            state = TreeScreenState.InitialLoad(
                nodeName = "0x000102030405060708090a0b0c0d0e0f10111213".toEthereumAddress(),
            ),
            labels = emptyFlow(),
            onIntent = { },
        )
    }
}

