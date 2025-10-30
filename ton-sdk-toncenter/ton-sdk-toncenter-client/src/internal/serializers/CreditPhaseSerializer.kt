package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.currency.CurrencyCollection
import org.ton.sdk.blockchain.currency.ExtraCurrencyCollection
import org.ton.sdk.blockchain.transaction.phases.CreditPhase

@OptIn(ExperimentalSerializationApi::class)
internal object CreditPhaseSerializer : KSerializer<CreditPhase> {
    private val coinsSerializer = CoinsSerializer
    private val extraCurrencySerializer = ExtraCurrencyCollectionSerializer

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        CreditPhaseSerializer::class.toString()
    ) {
        element("due_fees_collected", coinsSerializer.descriptor, isOptional = true)
        element("credit", coinsSerializer.descriptor, isOptional = true)
        element("credit_extra_currencies", extraCurrencySerializer.descriptor, isOptional = true)
    }

    override fun serialize(
        encoder: Encoder,
        value: CreditPhase
    ) = encoder.encodeStructure(descriptor) {
        encodeNullableSerializableElement(descriptor, 0, coinsSerializer, value.dueFeesCollected)
        encodeNullableSerializableElement(descriptor, 1, coinsSerializer, value.credit.coins)
        encodeNullableSerializableElement(descriptor, 2, extraCurrencySerializer, value.credit.extra)
    }

    override fun deserialize(decoder: Decoder): CreditPhase = decoder.decodeStructure(descriptor) {
        var dueFeesCollected: Coins? = null
        var creditCoins: Coins? = null
        var creditExtra: ExtraCurrencyCollection? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> dueFeesCollected = decodeNullableSerializableElement(descriptor, 0, coinsSerializer)
                1 -> creditCoins = decodeNullableSerializableElement(descriptor, 1, coinsSerializer)
                2 -> creditExtra = decodeNullableSerializableElement(descriptor, 2, extraCurrencySerializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw IllegalStateException("Unexpected index: $index")
            }
        }

        CreditPhase(
            dueFeesCollected = dueFeesCollected ?: Coins.ZERO,
            credit = CurrencyCollection(
                coins = creditCoins ?: Coins.ZERO,
                extra = creditExtra ?: ExtraCurrencyCollection.EMPTY
            )
        )
    }
}
