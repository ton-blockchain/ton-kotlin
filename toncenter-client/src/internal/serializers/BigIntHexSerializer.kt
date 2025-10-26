package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.sdk.bigint.BigInt
import org.ton.sdk.bigint.toBigInt
import org.ton.sdk.bigint.toString

internal object BigIntAsHexStringSerializer : KSerializer<BigInt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "org.ton.sdk.toncenter.internal.serializers.BigIntAsHexStringSerializer",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: BigInt) {
        encoder.encodeString("0x" + value.toString(16))
    }

    override fun deserialize(decoder: Decoder): BigInt {
        return decoder.decodeString().substringAfter("0x").toBigInt(16)
    }
}
