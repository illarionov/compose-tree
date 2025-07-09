package com.example.composetree.core.database.converter

import androidx.room.TypeConverter
import com.example.composetree.core.model.Address

class AddressConverter {
    @TypeConverter
    fun fromAddress(address: Address?): ByteArray? = address?.bytes?.toByteArray()

    @TypeConverter
    fun toAddress(bytes: ByteArray?) = bytes?.let { Address(it) }
}
