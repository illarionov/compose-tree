package com.example.composetree.feature.tree.data

import android.database.SQLException

internal fun SQLException.isUniqueConstraintFailed() =
    isConstraintViolation() && (message?.contains("UNIQUE constraint failed") ?: false)

internal fun SQLException.isConstraintViolation(): Boolean = message?.contains("Error code: 19") ?: false

