package org.ton.api.adnl.message

import kotlinx.serialization.SerialName
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

@SerialName("adnl.message.nop")
public object AdnlMessageNop : AdnlMessage, TlConstructor<AdnlMessageNop>(
    schema = "adnl.message.nop = adnl.Message"
) {
    public const val SIZE_BYTES: Int = 0

    override fun decode(reader: TlReader): AdnlMessageNop = this

    override fun encode(writer: TlWriter, value: AdnlMessageNop) {
    }
}
