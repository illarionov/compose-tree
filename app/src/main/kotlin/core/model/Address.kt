package com.example.composetree.core.model

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString

/**
 * Address - младшие 160 бит от 256-битного хэша Keccak-256 публичного ключа.
 *
 * Адреса уникальные.
 *
 * [bytes]: байтовое представление адреса, должен быть 20 байт.
 */
@JvmInline
value class Address(
    val bytes: ByteString,
) : Comparable<Address> {
    init {
        require(bytes.size == ADDRESS_SIZE_BYTES) { "Address must be $ADDRESS_SIZE_BYTES bytes long" }
    }

    /**
     * Представление адреса Etherum-формате, например "0xb794f5ea0ba39494ce839613fffba74279579268".
     *
     * Всегда 42 символа, начинается с 0x,
     */
    fun toEthereumString(): String = "0x${bytes.toHexString()}"

    override fun compareTo(other: Address): Int = this.bytes.compareTo(other.bytes)

    override fun toString(): String = toEthereumString()
}

/**
 * Размер адреса в байтах
 */
const val ADDRESS_SIZE_BYTES = 20

fun Address(bytes: ByteArray): Address = Address(ByteString(bytes))

/**
 * Возвращает адрес по строке в Etherum-формате адреса вида "0xb794f5ea0ba39494ce839613fffba74279579268"
 */
fun String.toAddress(): Address {
    require(this.startsWith("0x", ignoreCase = true)) { "Address must start with 0x" }
    val bytes = this.substring(2).hexToByteString()
    return Address(bytes)
}
