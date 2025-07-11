package com.example.composetree.feature.tree.presentation

import android.content.res.Resources
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.example.composetree.R
import com.example.composetree.core.model.EthereumAddress
import com.example.composetree.core.model.ROOT_NODE_NAME
import com.example.composetree.feature.tree.presentation.TreeScreenStore.SnackbarMessage

internal fun Resources.getString(message: SnackbarMessage): String = if (message.args.isEmpty()) {
    getString(message.resourceId)
} else {
    getString(message.resourceId, *message.args.toTypedArray())
}

@Composable
@ReadOnlyComposable
internal fun EthereumAddress.localizedText(): String = if (this == ROOT_NODE_NAME) {
    stringResource(R.string.root_node_app_bar_title)
} else {
    this.toEthereumString()
}

internal object EthereumAddressValidator {
    internal object EthereumAddressInputTransformation : InputTransformation {
        override fun TextFieldBuffer.transformInput() {
            val stringValue = toString()
            if (!isValidEthereumAddressPrefix(stringValue) || stringValue.length > 42) {
                revertAllChanges()
            }
        }
    }

    internal fun isValidEthereumAddress(address: String): Boolean {
        return isValidEthereumAddressPrefix(address) && address.length == 42
    }

    private fun isValidEthereumAddressPrefix(address: String): Boolean {
        return address.startsWith("0x", ignoreCase = true)
                && address.substring(2).all(::isHexChar)
    }

    private fun isHexChar(char: Char): Boolean = char in '0'..'9'
            || char in 'a'..'f'
            || char in 'A'..'F'
}
