package org.ton.kotlin.blockchain.message.address

import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.io.readULong
import kotlinx.io.write
import org.ton.kotlin.blockchain.ShardId
import org.ton.kotlin.crypto.HashBytes
import org.ton.kotlin.crypto.crc16
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmStatic

public sealed interface Address {
    public val workchain: Int

    public val prefix: ULong
}

public class AddressStd(
    public val anycast: Anycast?,
    public override val workchain: Int,
    public val address: HashBytes
) : Address {
    public constructor(workchain: Int, address: HashBytes) : this(null, workchain, address)

    public val isMasterchain: Boolean get() = workchain == ShardId.MASTERCHAIN.workchain

    public override val prefix: ULong
        get() = with(Buffer()) {
            write(address.value, 0, 8)
            readULong()
        }

    public fun toBase64String(bounceable: Boolean): String =
        toBase64String(Base64.Default, bounceable, false)

    public fun toBase64String(bounceable: Boolean, testnet: Boolean): String =
        toBase64String(Base64.Default, bounceable, testnet)

    public fun toBase64String(format: Base64, bounceable: Boolean, testnet: Boolean): String {
        val bytes = ByteArray(36)
        bytes[0] = (0x51 - (if (bounceable) 0x40 else 0) + (if (testnet) 0x80 else 0)).toByte()
        bytes[1] = workchain.toByte()
        address.value.copyInto(bytes, destinationOffset = 2)

        val crc = crc16(bytes.copyOf(34))
        bytes[34] = (crc ushr 8).toByte()
        bytes[35] = (crc and 0xFF).toByte()

        return format.encode(bytes)
    }

    public fun toRawString(): String = toRawString(HexFormat.Default)

    public fun toRawString(format: HexFormat): String = "$workchain:${address.value.toHexString(format)}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as AddressStd
        if (workchain != other.workchain) return false
        if (anycast != other.anycast) return false
        if (address != other.address) return false
        return true
    }

    override fun hashCode(): Int {
        var result = workchain
        result = 31 * result + (anycast?.hashCode() ?: 0)
        result = 31 * result + address.hashCode()
        return result
    }

    override fun toString(): String {
        if (anycast == null) {
            return "AddressStd(${toRawString()})"
        }
        return "AddressStd(anycast=$anycast,${toRawString()})"
    }

    public companion object {
        @JvmStatic
        public fun parse(addressString: String): AddressStd {
            return when (addressString.length) {
                48 -> addrStdParseBase64(addressString)
                in 66..69 -> addrStdParseHex(addressString)
                else -> throw IllegalArgumentException(
                    "Expected either a 48-char string in base64 format or a hexadecimal raw format, " +
                            "but was \"${addressString.truncateForErrorMessage(64)}\" of length ${addressString.length}"
                )
            }
        }

        @JvmStatic
        public fun parseRaw(addressString: String): AddressStd {
            require(addressString.length in 66..69) {
                "Expected a 66..69-char hexadecimal string, " +
                        "but was \"${addressString.truncateForErrorMessage(64)}\" " +
                        "of length ${addressString.length}"
            }
            return addrStdParseHex(addressString)
        }

        @JvmStatic
        public fun parseBase64(addressString: String): AddressStd {
            require(addressString.length == 48) {
                "Expected a 48-char base64 string, " +
                        "but was \"${addressString.truncateForErrorMessage(64)}\" " +
                        "of length ${addressString.length}"
            }
            return addrStdParseBase64(addressString)
        }
    }
}

private fun addrStdParseBase64(base64String: String): AddressStd {
    val format = if (base64String.indexOfAny(charArrayOf('_', '-')) != -1) {
        Base64.UrlSafe
    } else {
        Base64.Default
    }
    val bytes = format.decode(base64String)
    val crc = crc16(bytes.copyOf(34))
    require((bytes[34].toInt() and 0xFF) == (crc ushr 8) && bytes[35].toInt() and 0xFF == crc and 0xFF) {
        "Invalid crc"
    }
    return AddressStd(
        workchain = bytes[1].toInt(),
        address = HashBytes(ByteString(*bytes.copyOfRange(2, 34)))
    )
}

private fun addrStdParseHex(hexString: String): AddressStd {
    val (rawWorkchain, rawAddress) = hexString.split(":")
    return AddressStd(rawWorkchain.toInt(), HashBytes(rawAddress.hexToByteString()))
}

private fun String.truncateForErrorMessage(maxLength: Int): String {
    return if (length <= maxLength) this else substring(0, maxLength) + "..."
}
