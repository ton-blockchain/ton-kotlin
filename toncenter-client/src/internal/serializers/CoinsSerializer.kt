package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.sdk.bigint.toBigInt
import org.ton.sdk.blockchain.currency.Coins

internal object CoinsSerializer : KSerializer<Coins> {
    private val serializer = String.serializer()

    override val descriptor: SerialDescriptor =
        SerialDescriptor(CoinsSerializer::class.toString(), serializer.descriptor)

    override fun serialize(
        encoder: Encoder,
        value: Coins
    ) {
        serializer.serialize(encoder, value.value.toString())
    }

    override fun deserialize(decoder: Decoder): Coins {
        return Coins(serializer.deserialize(decoder).toBigInt())
    }
}
