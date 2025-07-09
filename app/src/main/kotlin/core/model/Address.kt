package com.example.composetree.core.model

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString


/**
 * Address - младшие 160 бит от 256-битного хэша Keccak-256 публичного ключа.
 *
 * [bytes]: байтовое представление адреса, 20 байт.
 */
@JvmInline
value class Address private constructor(
    public val bytes: ByteString
) {

    init {
        check(bytes.size == ADDRESS_SIZE_BYTES)
    }

    /**
     * Представление адреса Etherium-формате, например "0xb794f5ea0ba39494ce839613fffba74279579268".
     *
     * Всегда 42 символа, начинается с 0x,
     */
    public fun toEthereumString(): String {
        return bytes.toHexString(ETHERUM_FORMAT)
    }

    public companion object {
        /**
         * Размер адреса в байтах
         */
        public const val ADDRESS_SIZE_BYTES = 20

        private val ETHERUM_FORMAT = HexFormat {
            upperCase = false
            bytes.bytePrefix = "0x"
        }
    }
}
