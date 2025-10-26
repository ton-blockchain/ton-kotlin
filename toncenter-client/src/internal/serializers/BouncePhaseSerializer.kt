package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.*
import org.ton.sdk.blockchain.account.StorageUsedShort
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.transaction.phases.BouncePhase

@OptIn(ExperimentalSerializationApi::class)
internal object BouncePhaseSerializer : KSerializer<BouncePhase> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        BouncePhaseSerializer::class.toString()
    ) {
        element("type", String.serializer().descriptor)
        element("msg_size", StorageUsedShortSerializer.descriptor, isOptional = true)
        element("req_fwd_fees", CoinsSerializer.descriptor, isOptional = true)
        element("msg_fees", CoinsSerializer.descriptor, isOptional = true)
        element("fwd_fees", CoinsSerializer.descriptor, isOptional = true)
    }

    override fun serialize(
        encoder: Encoder,
        value: BouncePhase
    ) = encoder.encodeStructure(descriptor) {
        val type = type(value)
        val msgSize: StorageUsedShort?
        var reqFwdFees: Coins? = null
        var msgFees: Coins? = null
        var fwdFees: Coins? = null
        when (value) {
            is BouncePhase.Executed -> {
                msgSize = value.messageSize
                msgFees = value.messageFees
                fwdFees = value.forwardFees
            }

            is BouncePhase.NoFunds -> {
                msgSize = value.messageSize
                reqFwdFees = value.requiredForwardFees
            }
        }
        encodeStringElement(descriptor, 0, type)
        encodeNullableSerializableElement(descriptor, 1, StorageUsedShortSerializer, msgSize)
        encodeNullableSerializableElement(descriptor, 2, CoinsSerializer, reqFwdFees)
        encodeNullableSerializableElement(descriptor, 3, CoinsSerializer, msgFees)
        encodeNullableSerializableElement(descriptor, 4, CoinsSerializer, fwdFees)
    }

    override fun deserialize(decoder: Decoder): BouncePhase = decoder.decodeStructure(descriptor) {
        var type: String? = null
        var msgSize: StorageUsedShort? = null
        var reqFwdFees: Coins? = null
        var msgFees: Coins? = null
        var fwdFees: Coins? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> type = decodeStringElement(descriptor, 0)
                1 -> msgSize = decodeNullableSerializableElement(descriptor, 1, StorageUsedShortSerializer)
                2 -> reqFwdFees = decodeNullableSerializableElement(descriptor, 2, CoinsSerializer)
                3 -> msgFees = decodeNullableSerializableElement(descriptor, 3, CoinsSerializer)
                4 -> fwdFees = decodeNullableSerializableElement(descriptor, 4, CoinsSerializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw IllegalStateException("Unexpected index: $index")
            }
        }

        when (type) {
            "nofunds" -> BouncePhase.NoFunds(
                messageSize = msgSize ?: StorageUsedShort.ZERO,
                requiredForwardFees = reqFwdFees ?: Coins.ZERO
            )

            "ok" -> BouncePhase.Executed(
                messageSize = msgSize ?: StorageUsedShort.ZERO,
                messageFees = msgFees ?: Coins.ZERO,
                forwardFees = fwdFees ?: Coins.ZERO
            )

            "negfunds" -> throw UnsupportedOperationException("Unsupported BouncePhase type: $type")

            else -> throw IllegalStateException("Unknown BouncePhase type: $type")
        }
    }

    private fun type(bouncePhase: BouncePhase) = when (bouncePhase) {
        is BouncePhase.NoFunds -> "nofunds"
        is BouncePhase.Executed -> "ok"
    }
}
