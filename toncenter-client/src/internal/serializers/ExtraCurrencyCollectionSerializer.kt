package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.bigint.toBigInt
import org.ton.sdk.blockchain.currency.ExtraCoins
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection

internal object ExtraCurrencyCollectionSerializer : KSerializer<ExtraCurrencyCollection> {
    private val serializer = MapSerializer(
        String.serializer(),
        String.serializer()
    )

    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: ExtraCurrencyCollection
    ) {
        val rawMap = LinkedHashMap<String, String>()
        value.forEach { entry ->
            rawMap[entry.key.toString()] = entry.value.toString()
        }
        serializer.serialize(encoder, rawMap)
    }

    override fun deserialize(decoder: Decoder): ExtraCurrencyCollection {
        val rawMap = serializer.deserialize(decoder)
        val map = LinkedHashMap<Int, ExtraCoins>()
        rawMap.forEach { entry ->
            map[entry.key.toUInt().toInt()] = ExtraCoins(entry.value.toBigInt())
        }
        return ExtraCurrencyCollection(map)
    }
}
