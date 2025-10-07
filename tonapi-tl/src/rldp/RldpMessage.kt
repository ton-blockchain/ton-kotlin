@file:Suppress("OPT_IN_USAGE")

package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.tl.TlCombinator
import org.ton.tl.TlObject

@Polymorphic
@Serializable
@JsonClassDiscriminator("@type")
public sealed interface RldpMessage : TlObject<RldpMessage> {
    public val id: ByteString
    public val data: ByteString

    public companion object : TlCombinator<RldpMessage>(
        RldpMessage::class,
        RldpMessageData::class to RldpMessageData,
        RldpQuery::class to RldpQuery,
        RldpAnswer::class to RldpAnswer,
    )
}
