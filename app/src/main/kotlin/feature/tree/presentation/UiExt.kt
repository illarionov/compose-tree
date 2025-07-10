package com.example.composetree.feature.tree.presentation

import android.content.res.Resources
import com.example.composetree.feature.tree.presentation.TreeScreenStore.SnackbarMessage

internal fun Resources.getString(message: SnackbarMessage): String = if (message.args.isEmpty()) {
    getString(message.resourceId)
} else {
    getString(message.resourceId, *message.args.toTypedArray())
}
