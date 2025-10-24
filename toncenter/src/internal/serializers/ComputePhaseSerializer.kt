package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import org.ton.kotlin.crypto.HashBytes
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.transaction.phases.ComputePhase


@OptIn(ExperimentalSerializationApi::class)
internal object ComputePhaseSerializer : KSerializer<ComputePhase> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ComputePhase") {
        element<Boolean>("skipped")
        element<String?>("reason", isOptional = true)
        element<Boolean?>("success", isOptional = true)
        element<Boolean?>("msg_state_used", isOptional = true)
        element<Boolean?>("account_activated", isOptional = true)
        element("gas_fees", CoinsSerializer.descriptor, isOptional = true)
        element<Long?>("gas_used", isOptional = true)
        element<Long?>("gas_limit", isOptional = true)
        element<Int?>("gas_credit", isOptional = true)
        element<Byte?>("mode", isOptional = true)
        element<Int?>("exit_code", isOptional = true)
        element<Int?>("exit_arg", isOptional = true)
        element<Int?>("vm_steps", isOptional = true)
        element("vm_init_state_hash", HashBytesAsBase64Serializer.descriptor, isOptional = true)
        element("vm_final_state_hash", HashBytesAsBase64Serializer.descriptor, isOptional = true)
    }

    @Suppress("DuplicatedCode")
    override fun serialize(
        encoder: Encoder,
        value: ComputePhase
    ) = encoder.encodeStructure(descriptor) {
        val isSkipped = value !is ComputePhase.Executed
        val reason = when (value.skipReason) {
            ComputePhase.Skipped.NO_STATE -> "no_state"
            ComputePhase.Skipped.BAD_STATE -> "bad_state"
            ComputePhase.Skipped.NO_GAS -> "no_gas"
            ComputePhase.Skipped.SUSPENDED -> "suspended"
            null -> null
        }
        var success: Boolean? = null
        var msgStateUsed: Boolean? = null
        var accountActivated: Boolean? = null
        var gasFees: Coins? = null
        var gasUsed: Long? = null
        var gasLimit: Long? = null
        var gasCredit: Int? = null
        var mode: Byte? = null
        var exitCode: Int? = null
        var exitArg: Int? = null
        var vmSteps: Int? = null
        var vmInitStateHash: HashBytes? = null
        var vmFinalStateHash: HashBytes? = null
        if (!isSkipped) {
            success = value.isSuccess
            msgStateUsed = value.isMsgStateUsed
            accountActivated = value.isAccountActivated
            gasFees = value.gasFees
            gasUsed = value.gasUsed
            gasLimit = value.gasLimit
            gasCredit = value.gasCredit
            mode = value.mode
            exitCode = value.exitCode
            exitArg = value.exitArg
            vmSteps = value.vmSteps
            vmInitStateHash = value.vmInitStateHash
            vmFinalStateHash = value.vmFinalStateHash
        }
        encodeBooleanElement(descriptor, 0, isSkipped)
        encodeNullableSerializableElement(descriptor, 1, String.serializer(), reason)
        encodeNullableSerializableElement(descriptor, 2, Boolean.serializer(), success)
        encodeNullableSerializableElement(descriptor, 3, Boolean.serializer(), msgStateUsed)
        encodeNullableSerializableElement(descriptor, 4, Boolean.serializer(), accountActivated)
        encodeNullableSerializableElement(descriptor, 5, CoinsSerializer, gasFees)
        encodeNullableSerializableElement(descriptor, 6, Long.serializer(), gasUsed)
        encodeNullableSerializableElement(descriptor, 7, Long.serializer(), gasLimit)
        encodeNullableSerializableElement(descriptor, 8, Int.serializer(), gasCredit)
        encodeNullableSerializableElement(descriptor, 9, Byte.serializer(), mode)
        encodeNullableSerializableElement(descriptor, 10, Int.serializer(), exitCode)
        encodeNullableSerializableElement(descriptor, 11, Int.serializer(), exitArg)
        encodeNullableSerializableElement(descriptor, 12, Int.serializer(), vmSteps)
        encodeNullableSerializableElement(descriptor, 13, HashBytesAsBase64Serializer, vmInitStateHash)
        encodeNullableSerializableElement(descriptor, 14, HashBytesAsBase64Serializer, vmFinalStateHash)
    }

    @Suppress("DuplicatedCode")
    override fun deserialize(decoder: Decoder): ComputePhase = decoder.decodeStructure(descriptor) {
        var isSkipped = false
        var reason: String? = null
        var success: Boolean? = null
        var msgStateUsed: Boolean? = null
        var accountActivated: Boolean? = null
        var gasFees: Coins? = null
        var gasUsed: Long? = null
        var gasLimit: Long? = null
        var gasCredit: Int? = null
        var mode: Byte? = null
        var exitCode: Int? = null
        var exitArg: Int? = null
        var vmSteps: Int? = null
        var vmInitStateHash: HashBytes? = null
        var vmFinalStateHash: HashBytes? = null

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> isSkipped = decodeBooleanElement(descriptor, 0)
                1 -> reason = decodeNullableSerializableElement(descriptor, 1, String.serializer())
                2 -> success = decodeNullableSerializableElement(descriptor, 2, Boolean.serializer())
                3 -> msgStateUsed = decodeNullableSerializableElement(descriptor, 3, Boolean.serializer())
                4 -> accountActivated = decodeNullableSerializableElement(descriptor, 4, Boolean.serializer())
                5 -> gasFees = decodeNullableSerializableElement(descriptor, 5, CoinsSerializer)
                6 -> gasUsed = decodeNullableSerializableElement(descriptor, 6, Long.serializer())
                7 -> gasLimit = decodeNullableSerializableElement(descriptor, 7, Long.serializer())
                8 -> gasCredit = decodeNullableSerializableElement(descriptor, 8, Int.serializer())
                9 -> mode = decodeNullableSerializableElement(descriptor, 9, Byte.serializer())
                10 -> exitCode = decodeNullableSerializableElement(descriptor, 10, Int.serializer())
                11 -> exitArg = decodeNullableSerializableElement(descriptor, 11, Int.serializer())
                12 -> vmSteps = decodeNullableSerializableElement(descriptor, 12, Int.serializer())
                13 -> vmInitStateHash = decodeNullableSerializableElement(descriptor, 13, HashBytesAsBase64Serializer)
                14 -> vmFinalStateHash = decodeNullableSerializableElement(descriptor, 14, HashBytesAsBase64Serializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw IllegalStateException("Unexpected index: $index")
            }
        }

        if (isSkipped) {
            when (reason) {
                "no_state" -> ComputePhase.Skipped.NO_STATE
                "bad_state" -> ComputePhase.Skipped.BAD_STATE
                "no_gas" -> ComputePhase.Skipped.NO_GAS
                "suspended" -> ComputePhase.Skipped.SUSPENDED
                else -> throw IllegalStateException("Unexpected skip reason: $reason")
            }
        } else {
            ComputePhase.Executed(
                isSuccess = success ?: false,
                isMsgStateUsed = msgStateUsed ?: false,
                isAccountActivated = accountActivated ?: false,
                gasFees = gasFees ?: Coins.ZERO,
                gasUsed = gasUsed ?: 0L,
                gasLimit = gasLimit ?: 0L,
                gasCredit = gasCredit,
                mode = mode ?: 0,
                exitCode = exitCode ?: 0,
                exitArg = exitArg,
                vmSteps = vmSteps ?: throw MissingFieldException("vm_steps", descriptor.serialName),
                vmInitStateHash = vmInitStateHash ?: throw MissingFieldException(
                    "vm_init_state_hash",
                    descriptor.serialName
                ),
                vmFinalStateHash = vmFinalStateHash ?: throw MissingFieldException(
                    "vm_final_state_hash",
                    descriptor.serialName
                )
            )
        }
    }
}
