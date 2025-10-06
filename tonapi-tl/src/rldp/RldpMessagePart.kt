package org.ton.api.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlCombinator
import org.ton.kotlin.tl.TlObject

@Serializable
public sealed interface RldpMessagePart : TlObject<RldpMessagePart> {
    public val transferId: ByteString
    public val part: Int

    public companion object : TlCombinator<RldpMessagePart>(
        RldpMessagePart::class,
        RldpMessagePartData::class to RldpMessagePartData,
        RldpConfirm::class to RldpConfirm,
        RldpComplete::class to RldpComplete,
    )
}
