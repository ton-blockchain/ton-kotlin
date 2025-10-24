package org.ton.sdk.toncenter.internal.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.sdk.blockchain.account.AccountStatus

internal object AccountStatusSerializer : KSerializer<AccountStatus> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        AccountStatusSerializer::class.toString(),
        PrimitiveKind.STRING
    )

    override fun serialize(
        encoder: Encoder,
        value: AccountStatus
    ) {
        val value = when (value) {
            AccountStatus.ACTIVE -> "active"
            AccountStatus.FROZEN -> "frozen"
            AccountStatus.NONEXIST -> "nonexist"
            AccountStatus.UNINIT -> "uninit"
        }
        encoder.encodeString(value)
    }

    override fun deserialize(decoder: Decoder): AccountStatus {
        val string = decoder.decodeString()
        return parseAccountStatus(string)
            ?: parseAccountStatus(string.lowercase())
            ?: throw IllegalArgumentException("Unknown AccountStatus value: $string")
    }

    private fun parseAccountStatus(string: String) = when (string) {
        "active" -> AccountStatus.ACTIVE
        "frozen" -> AccountStatus.FROZEN
        "nonexist" -> AccountStatus.NONEXIST
        "uninit" -> AccountStatus.UNINIT
        else -> null
    }
}
