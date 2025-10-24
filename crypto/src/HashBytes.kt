package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import kotlin.jvm.JvmStatic

@Serializable
public class HashBytes(
    @Serializable(ByteStringBase64Serializer::class)
    @Bits256
    public val value: ByteString
) {
    public constructor(array: ByteArray) : this(ByteString(array))

    init {
        require(value.size == 32)
    }

    override fun toString(): String = value.toHexString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as HashBytes
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    public companion object {
        @JvmStatic
        public fun parseHex(hexString: String): HashBytes {
            require(hexString.length == 64 || (hexString.startsWith("0x") && hexString.length == 66)) {
                "Hex string must be 64 characters long or 66 characters long with the `0x` prefix"
            }
            return HashBytes(hexString.removePrefix("0x").hexToByteString())
        }
    }
}
