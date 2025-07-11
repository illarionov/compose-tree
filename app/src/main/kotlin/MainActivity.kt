package com.example.composetree

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composetree.core.ui.theme.ComposeTreeTheme
import com.example.composetree.feature.tree.presentation.NodeTreeRoot
import com.example.composetree.feature.tree.presentation.NodeTreeViewModel

class MainActivity : ComponentActivity() {
    private val treeViewModel: NodeTreeViewModel by viewModels { NodeTreeViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // https://issuetracker.google.com/issues/326356902
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            ComposeTreeTheme {
                val state by treeViewModel.screenState.collectAsStateWithLifecycle()

                NodeTreeRoot(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    labels = treeViewModel.labels,
                    onIntent = treeViewModel::acceptIntent,
                )
            }
        }
    }
}

