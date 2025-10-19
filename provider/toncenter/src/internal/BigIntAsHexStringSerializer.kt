package org.ton.kotlin.provider.toncenter.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.bigint.BigInt
import org.ton.bigint.toBigInt

public object BigIntAsStringSerializer : KSerializer<BigInt> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        "org.ton.kotlin.provider.toncenter.internal.BigIntAsStringSerializer",
        PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: BigInt) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigInt {
        return decoder.decodeString().toBigInt()
    }
}
