@file:Suppress("OPT_IN_USAGE")

package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import org.ton.sdk.blockchain.transaction.TransactionDescription
import org.ton.sdk.blockchain.transaction.phases.*

internal object TransactionDescriptionSerializer : KSerializer<TransactionDescription> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor(
        TransactionDescriptionSerializer::class.toString()
    ) {
        element<String>("type")
        element<Boolean?>("aborted", isOptional = true)
        element<Boolean?>("destroyed", isOptional = true)
        element<Boolean?>("credit_first", isOptional = true)
        element<Boolean?>("is_tock", isOptional = true)
        element<Boolean?>("installed", isOptional = true)
        element("storage_ph", isOptional = true, descriptor = StoragePhaseSerializer.descriptor)
        element("credit_ph", isOptional = true, descriptor = CreditPhaseSerializer.descriptor)
        element("compute_ph", isOptional = true, descriptor = ComputePhaseSerializer.descriptor)
        element("action", isOptional = true, descriptor = ActionPhaseSerializer.descriptor)
        element("bounce", isOptional = true, descriptor = BouncePhaseSerializer.descriptor)
    }

    override fun serialize(
        encoder: Encoder,
        value: TransactionDescription
    ) = encoder.encodeStructure(descriptor) {
        val type: String
        val aborted: Boolean?
        val destroyed: Boolean?
        var creditFirst: Boolean? = null
        var isTock: Boolean? = null
        val storagePh: StoragePhase?
        val computePh: ComputePhase?
        var creditPhase: CreditPhase? = null
        val action: ActionPhase?
        var bounce: BouncePhase? = null
        when (value) {
            is TransactionDescription.Ordinary -> {
                type = "ord"
                creditFirst = value.isCreditFirst
                storagePh = value.storagePhase
                creditPhase = value.creditPhase
                computePh = value.computePhase
                action = value.actionPhase
                aborted = value.isAborted
                bounce = value.bouncePhase
                destroyed = value.isDestroyed
            }

            is TransactionDescription.TickTock -> {
                type = "tick_tock"
                isTock = value.isTock
                storagePh = value.storagePhase
                computePh = value.computePhase
                action = value.actionPhase
                aborted = value.isAborted
                destroyed = value.isDestroyed
            }

            else -> throw NotImplementedError("Deserialization for type ${value::class.simpleName} is not implemented yet")
        }
        encodeStringElement(descriptor, 0, type)
        encodeNullableSerializableElement(descriptor, 1, Boolean.serializer(), aborted)
        encodeNullableSerializableElement(descriptor, 2, Boolean.serializer(), destroyed)
        encodeNullableSerializableElement(descriptor, 3, Boolean.serializer(), creditFirst)
        encodeNullableSerializableElement(descriptor, 4, Boolean.serializer(), isTock)
        encodeNullableSerializableElement(descriptor, 5, Boolean.serializer(), null)
        encodeNullableSerializableElement(descriptor, 6, StoragePhaseSerializer, storagePh)
        encodeNullableSerializableElement(descriptor, 7, CreditPhaseSerializer, creditPhase)
        encodeNullableSerializableElement(descriptor, 8, ComputePhaseSerializer, computePh)
        encodeNullableSerializableElement(descriptor, 9, ActionPhaseSerializer, action)
        encodeNullableSerializableElement(descriptor, 10, BouncePhaseSerializer, bounce)
    }

    override fun deserialize(decoder: Decoder): TransactionDescription = decoder.decodeStructure(descriptor) {
        var type: String? = null
        var aborted: Boolean? = null
        var destroyed: Boolean? = null
        var creditFirst: Boolean? = null
        var isTock: Boolean? = null
        var storagePh: StoragePhase? = null
        var computePh: ComputePhase? = null
        var creditPhase: CreditPhase? = null
        var action: ActionPhase? = null
        var bounce: BouncePhase? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> type = decodeStringElement(descriptor, 0)
                1 -> aborted = decodeNullableSerializableElement(descriptor, 1, Boolean.serializer())
                2 -> destroyed = decodeNullableSerializableElement(descriptor, 2, Boolean.serializer())
                3 -> creditFirst = decodeNullableSerializableElement(descriptor, 3, Boolean.serializer())
                4 -> isTock = decodeNullableSerializableElement(descriptor, 4, Boolean.serializer())
                6 -> storagePh = decodeNullableSerializableElement(descriptor, 6, StoragePhaseSerializer)
                7 -> creditPhase = decodeNullableSerializableElement(descriptor, 7, CreditPhaseSerializer)
                8 -> computePh = decodeNullableSerializableElement(descriptor, 8, ComputePhaseSerializer)
                9 -> action = decodeNullableSerializableElement(descriptor, 9, ActionPhaseSerializer)
                10 -> bounce = decodeNullableSerializableElement(descriptor, 10, BouncePhaseSerializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw IllegalStateException("Unexpected index: $index")
            }
        }

        when (type) {
            "ord" -> TransactionDescription.Ordinary(
                isCreditFirst = creditFirst ?: false,
                storagePhase = storagePh,
                creditPhase = creditPhase,
                computePhase = computePh ?: throw MissingFieldException("compute_ph", descriptor.serialName),
                actionPhase = action,
                isAborted = aborted ?: false,
                bouncePhase = bounce,
                isDestroyed = destroyed ?: false
            )

            "tick_tock" -> TransactionDescription.TickTock(
                isTock = isTock ?: false,
                storagePhase = storagePh ?: throw MissingFieldException("storage_ph", descriptor.serialName),
                computePhase = computePh ?: throw MissingFieldException("compute_ph", descriptor.serialName),
                actionPhase = action,
                isAborted = aborted ?: false,
                isDestroyed = destroyed ?: false
            )

            else -> throw NotImplementedError("Deserialization for type $type is not implemented yet")
        }
    }
}
