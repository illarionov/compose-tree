package com.example.composetree.feature.tree.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.composetree.R
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.core.ui.theme.ComposeTreeTheme
import com.example.composetree.feature.tree.presentation.EthereumAddressValidator.EthereumAddressInputTransformation
import com.example.composetree.feature.tree.presentation.TreeScreenStore.Intent
import com.example.composetree.feature.tree.presentation.TreeScreenStore.TreeScreenState.InsertNodeDialogState

@Composable
internal fun InsertNodeDialog(
    parentNodeName: EthereumAddress,
    state: InsertNodeDialogState,
    onIntent: (Intent) -> Unit,
) {
    val sendDismissEvent = { onIntent(Intent.DismissInsertNodeDialog) }
    val isNameInputValid: Boolean by remember {
        derivedStateOf {
            EthereumAddressValidator.isValidEthereumAddress(state.textFieldState.text.toString())
        }
    }

    Dialog(
        onDismissRequest = sendDismissEvent,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(305.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.insert_node_dialog_add_node),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                )
                Text(
                    text = stringResource(R.string.insert_node_dialog_label_parent_node),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = parentNodeName.localizedText(),
                    style = MaterialTheme.typography.bodySmall,
                )

                TextField(
                    state = state.textFieldState,
                    label = {
                        Text(stringResource(R.string.insert_node_dialog_label_node_name))
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                    inputTransformation = EthereumAddressInputTransformation,
                    isError = !isNameInputValid,
                    supportingText = {
                        if (!isNameInputValid) {
                            Text(stringResource(R.string.input_is_not_valid_ethereum_address))
                        }
                    },
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = sendDismissEvent,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(stringResource(R.string.dialog_dismiss))
                    }
                    TextButton(
                        onClick = {
                            onIntent(
                                Intent.ConfirmInsertNode(
                                    parent = parentNodeName,
                                    name = state.textFieldState.text.toString(),
                                ),
                            )
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(stringResource(R.string.insert_node_dialog_button_confirm))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InsertNodeDialogPreview() {
    ComposeTreeTheme {
        InsertNodeDialog(
            parentNodeName = ROOT_NODE_NAME,
            state = InsertNodeDialogState(
                textFieldState = TextFieldState(initialText = "0xb794f5ea0ba39494ce839613fffba74279579268"),
            ),
            onIntent = { },
        )
    }
}

