package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.sdk.blockchain.transaction.phases.AccountStatusChange

internal object AccountStatusChangeSerializer : KSerializer<AccountStatusChange> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        AccountStatusChangeSerializer::class.toString(),
        PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: AccountStatusChange
    ) {
        val value = when (value) {
            AccountStatusChange.UNCHANGED -> "unchanged"
            AccountStatusChange.FROZEN -> "frozen"
            AccountStatusChange.DELETED -> "deleted"
        }
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): AccountStatusChange {
        val string = decoder.decodeString()
        return parseAccountStatusChange(string)
            ?: parseAccountStatusChange(string.lowercase())
            ?: throw IllegalArgumentException("Unknown AccountStatusChange value: $string")
    }

    private fun parseAccountStatusChange(string: String) = when (string) {
        "unchanged" -> AccountStatusChange.UNCHANGED
        "frozen" -> AccountStatusChange.FROZEN
        "deleted" -> AccountStatusChange.DELETED
        else -> null
    }
}
