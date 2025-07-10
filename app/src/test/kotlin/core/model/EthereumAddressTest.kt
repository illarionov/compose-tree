package com.example.composetree.core.model

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import assertk.tableOf
import kotlinx.io.bytestring.ByteString
import org.junit.Test

class EthereumAddressTest {
    @Test
    fun address_constructor_success_case() {
        val validAddress = ByteString(ByteArray(20, Int::toByte))
        val ethereumAddress = EthereumAddress(validAddress)
        assertThat(ethereumAddress.bytes).isEqualTo(validAddress)
    }

    @Test
    fun address_should_throw_on_invalid_address() {
        val malformedAddress = ByteArray(1, Int::toByte)
        assertFailure { EthereumAddress(malformedAddress) }.hasClass(IllegalArgumentException::class)
    }

    @Test
    fun toEthereumString_should_return_correct_etherum_value() {
        tableOf("bytes", "expectedString")
            .row(ByteArray(20, Int::toByte), "0x000102030405060708090a0b0c0d0e0f10111213")
            .row(ByteArray(20) { 0.toByte() }, "0x0000000000000000000000000000000000000000")
            .row(ByteArray(20) { if (it > 16) it.toByte() else 0 }, "0x0000000000000000000000000000000000111213")
            .forAll { bytes, expectedResult ->
                val address = EthereumAddress(bytes)
                assertThat(address.toEthereumString()).isEqualTo(expectedResult)
            }
    }

    @Test
    fun toAddress_success_case() {
        tableOf("stringAddress", "expectedAddress")
            .row("0x000102030405060708090a0b0c0d0e0f10111213", ByteArray(20, Int::toByte))
            .row(
                "0X00aaBbCCddEEff00000000000000000000000001",
                "00aabbccddeeff00000000000000000000000001".hexToByteArray(),
            )
            .row("0x0000000000000000000000000000000000000000", ByteArray(20) { 0.toByte() })
            .row("0x0000000000000000000000000000000000111213", ByteArray(20) { if (it > 16) it.toByte() else 0 })
            .forAll { etherumAddress, expectedAddressBytes ->
                assertThat(etherumAddress.toEthereumAddress()).isEqualTo(EthereumAddress(expectedAddressBytes))
            }
    }

    @Test
    fun toAddress_fail_case() {
        tableOf("malformedAddress")
            .row("")
            .row(" ")
            .row("00")
            .row("000102030405060708090a0b0c0d0e0f10111213")
            .row("h00102030405060708090a0b0c0d0e0f10111213")
            .row("x00102030405060708090a0b0c0d0e0f10111213")
            .forAll { malformedAddress ->
                assertFailure { malformedAddress.toEthereumAddress() }.hasClass(IllegalArgumentException::class)
            }
    }

}
