package com.example.composetree.core.model

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

/**
 * Address - младшие 160 бит от 256-битного хэша Keccak-256 публичного ключа.
 *
 * Адреса уникальные.
 *
 * [bytes]: байтовое представление адреса, должен быть 20 байт.
 */
@JvmInline
@Parcelize // XXX: этого здесь быть не должно, но пока используем, так как работаем только Android
value class EthereumAddress(
    val bytes: ByteString,
) : Comparable<EthereumAddress>, Parcelable {
    init {
        require(bytes.size == ADDRESS_SIZE_BYTES) { "Address must be $ADDRESS_SIZE_BYTES bytes long" }
    }

    /**
     * Представление адреса Etherum-формате, например "0xb794f5ea0ba39494ce839613fffba74279579268".
     *
     * Всегда 42 символа, начинается с 0x,
     */
    fun toEthereumString(): String = "0x${bytes.toHexString()}"

    override fun compareTo(other: EthereumAddress): Int = this.bytes.compareTo(other.bytes)

    override fun toString(): String = toEthereumString()

    private companion object : Parceler<EthereumAddress> {
        override fun EthereumAddress.write(parcel: Parcel, flags: Int) {
            parcel.writeByteArray(this.bytes.toByteArray())
        }

        override fun create(parcel: Parcel): EthereumAddress {
            val bytes = ByteArray(ADDRESS_SIZE_BYTES).also(parcel::readByteArray)
            return EthereumAddress(bytes)
        }
    }
}

/**
 * Размер адреса в байтах
 */
const val ADDRESS_SIZE_BYTES = 20

fun EthereumAddress(bytes: ByteArray): EthereumAddress = EthereumAddress(ByteString(bytes))

/**
 * Возвращает адрес по строке в Etherum-формате адреса вида "0xb794f5ea0ba39494ce839613fffba74279579268"
 */
fun String.toEthereumAddress(): EthereumAddress {
    require(this.startsWith("0x", ignoreCase = true)) { "Address must start with 0x" }
    val bytes = this.substring(2).hexToByteString()
    return EthereumAddress(bytes)
}
