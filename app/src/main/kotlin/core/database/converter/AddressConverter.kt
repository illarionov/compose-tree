package com.example.composetree.core.database.converter

import androidx.room.TypeConverter
import com.example.composetree.core.model.EthereumAddress

class AddressConverter {
    @TypeConverter
    fun fromAddress(ethereumAddress: EthereumAddress?): ByteArray? = ethereumAddress?.bytes?.toByteArray()

    @TypeConverter
    fun toAddress(bytes: ByteArray?) = bytes?.let { EthereumAddress(it) }
}
