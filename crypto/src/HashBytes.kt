package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public class HashBytes(
    @Serializable(ByteStringBase64Serializer::class)
    @Bits256
    public val value: ByteString
) {
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
}
