package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import org.ton.kotlin.crypto.HashBytes
import org.ton.sdk.blockchain.account.StorageUsedShort
import org.ton.sdk.blockchain.currency.Coins
import org.ton.sdk.blockchain.transaction.phases.AccountStatusChange
import org.ton.sdk.blockchain.transaction.phases.ActionPhase

@OptIn(ExperimentalSerializationApi::class)
internal object ActionPhaseSerializer : KSerializer<ActionPhase> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ActionPhase") {
        element<Boolean>("success")
        element<Boolean>("valid")
        element<Boolean>("no_funds")
        element<String>("status_change")
        element("total_fwd_fees", CoinsSerializer.descriptor, isOptional = true)
        element("total_action_fees", CoinsSerializer.descriptor, isOptional = true)
        element<Int>("result_code")
        element<Int>("result_arg")
        element<Int>("tot_actions")
        element<Int>("spec_actions")
        element<Int>("skipped_actions")
        element<Int>("msgs_created")
        element("action_list_hash", HashBytesAsBase64Serializer.descriptor)
        element("tot_msg_size", StorageUsedShortSerializer.descriptor)
    }

    override fun serialize(
        encoder: Encoder,
        value: ActionPhase
    ) = encoder.encodeStructure(descriptor) {
        encodeBooleanElement(descriptor, 0, value.isSuccess)
        encodeBooleanElement(descriptor, 1, value.isValid)
        encodeBooleanElement(descriptor, 2, value.noFunds)
        encodeStringElement(descriptor, 3, statusChange(value.statusChange))
        encodeNullableSerializableElement(descriptor, 4, CoinsSerializer, value.totalFwdFees)
        encodeNullableSerializableElement(descriptor, 5, CoinsSerializer, value.totalActionFees)
        encodeIntElement(descriptor, 6, value.resultCode)
        encodeNullableSerializableElement(descriptor, 7, Int.serializer(), value.resultArg)
        encodeIntElement(descriptor, 8, value.totalActions)
        encodeIntElement(descriptor, 9, value.specialActions)
        encodeIntElement(descriptor, 10, value.skippedActions)
        encodeIntElement(descriptor, 11, value.messagesCreated)
        encodeSerializableElement(descriptor, 12, HashBytesAsBase64Serializer, value.actionListHash)
        encodeSerializableElement(descriptor, 13, StorageUsedShortSerializer, value.totalMessageSize)
    }

    override fun deserialize(decoder: Decoder): ActionPhase = decoder.decodeStructure(descriptor) {
        var isSuccess = false
        var isValid = false
        var noFunds = false
        var statusChange: AccountStatusChange = AccountStatusChange.UNCHANGED
        var totalFwdFees: Coins? = null
        var totalActionFees: Coins? = null
        var resultCode = 0
        var resultArg: Int? = null
        var totalActions = 0
        var specialActions = 0
        var skippedActions = 0
        var messagesCreated = 0
        lateinit var actionListHash: HashBytes
        lateinit var totalMessageSize: StorageUsedShort

        while (true) {
            when (val index = decodeElementIndex(descriptor)) {
                0 -> isSuccess = decodeBooleanElement(descriptor, 0)
                1 -> isValid = decodeBooleanElement(descriptor, 1)
                2 -> noFunds = decodeBooleanElement(descriptor, 2)
                3 -> {
                    val string = decodeStringElement(descriptor, 3)
                    statusChange = parseStatusChange(string)
                        ?: parseStatusChange(string.lowercase())
                                ?: throw SerializationException("Unknown AccountStatusChange: `$string`")
                }

                4 -> totalFwdFees = decodeNullableSerializableElement(descriptor, 4, CoinsSerializer)
                5 -> totalActionFees = decodeNullableSerializableElement(descriptor, 5, CoinsSerializer)
                6 -> resultCode = decodeIntElement(descriptor, 6)
                7 -> resultArg = decodeNullableSerializableElement(descriptor, 7, Int.serializer())
                8 -> totalActions = decodeIntElement(descriptor, 8)
                9 -> specialActions = decodeIntElement(descriptor, 9)
                10 -> skippedActions = decodeIntElement(descriptor, 10)
                11 -> messagesCreated = decodeIntElement(descriptor, 11)
                12 -> actionListHash = decodeSerializableElement(descriptor, 12, HashBytesAsBase64Serializer)
                13 -> totalMessageSize = decodeSerializableElement(descriptor, 13, StorageUsedShortSerializer)
                CompositeDecoder.DECODE_DONE -> break
                else -> throw IllegalStateException("Unexpected index: $index")
            }
        }

        ActionPhase(
            isSuccess = isSuccess,
            isValid = isValid,
            noFunds = noFunds,
            statusChange = statusChange,
            totalFwdFees = totalFwdFees,
            totalActionFees = totalActionFees,
            resultCode = resultCode,
            resultArg = resultArg,
            totalActions = totalActions,
            specialActions = specialActions,
            skippedActions = skippedActions,
            messagesCreated = messagesCreated,
            actionListHash = actionListHash,
            totalMessageSize = totalMessageSize
        )
    }

    private fun statusChange(statusChange: AccountStatusChange) = when (statusChange) {
        AccountStatusChange.UNCHANGED -> "unchanged"
        AccountStatusChange.FROZEN -> "frozen"
        AccountStatusChange.DELETED -> "deleted"
    }

    private fun parseStatusChange(string: String) = when (string) {
        "unchanged" -> AccountStatusChange.UNCHANGED
        "frozen" -> AccountStatusChange.FROZEN
        "deleted" -> AccountStatusChange.DELETED
        else -> null
    }
}
