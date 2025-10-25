package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.transaction.phases.AccountStatusChange
import org.ton.sdk.blockchain.transaction.phases.StoragePhase

@OptIn(ExperimentalSerializationApi::class)
internal object StoragePhaseSerializer : KSerializer<StoragePhase> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        StoragePhaseSerializer::class.toString()
    ) {
        element("storage_fees_collected", CoinsSerializer.descriptor, isOptional = true)
        element("storage_fees_due", CoinsSerializer.descriptor, isOptional = true)
        element<AccountStatusChange?>("status_change", isOptional = true)
    }

    override fun serialize(
        encoder: Encoder,
        value: StoragePhase
    ) = encoder.encodeStructure(descriptor) {
        encodeNullableSerializableElement(descriptor, 0, CoinsSerializer, value.storageFeesCollected)
        encodeNullableSerializableElement(descriptor, 1, CoinsSerializer, value.storageFeesDue)
        encodeNullableSerializableElement(descriptor, 2, AccountStatusChangeSerializer, value.statusChange)
    }

    override fun deserialize(decoder: Decoder): StoragePhase = decoder.decodeStructure(descriptor) {
        var storageFeesCollected: Coins? = null
        var storageFeesDue: Coins? = null
        var statusChange: AccountStatusChange? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> storageFeesCollected = decodeNullableSerializableElement(descriptor, 0, CoinsSerializer)
                1 -> storageFeesDue = decodeNullableSerializableElement(descriptor, 1, CoinsSerializer)
                2 -> statusChange = decodeNullableSerializableElement(descriptor, 2, AccountStatusChangeSerializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw IllegalStateException("Unexpected index: $index")
            }
        }

        StoragePhase(
            storageFeesCollected = storageFeesCollected ?: Coins.ZERO,
            storageFeesDue = storageFeesDue,
            statusChange = statusChange ?: AccountStatusChange.UNCHANGED
        )
    }
}
