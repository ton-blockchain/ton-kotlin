package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import org.ton.sdk.blockchain.account.StorageUsedShort

internal object StorageUsedShortSerializer : KSerializer<StorageUsedShort> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        StorageUsedShortSerializer::class.toString()
    ) {
        element<Long>("cells")
        element<Long>("bits")
    }

    override fun serialize(
        encoder: Encoder,
        value: StorageUsedShort
    ) = encoder.encodeStructure(descriptor) {
        encodeLongElement(descriptor, 0, value.cells)
        encodeLongElement(descriptor, 1, value.bits)
    }

    override fun deserialize(decoder: Decoder): StorageUsedShort = decoder.decodeStructure(descriptor) {
        var cells: Long? = null
        var bits: Long? = null
        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> cells = decodeLongElement(descriptor, 0)
                1 -> bits = decodeLongElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw IllegalStateException("Unexpected index: $index")
            }
        }
        StorageUsedShort(
            cells = cells ?: 0,
            bits = bits ?: 0
        )
    }
}
