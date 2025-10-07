package org.ton.api.adnl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmName

@Serializable
@SerialName("adnl.ping")
public data class AdnlPing(
    @get:JvmName("value")
    val value: Long
) {
    public companion object : TlConstructor<AdnlPing>(
        "adnl.ping value:long = adnl.Pong"
    ) {
        override fun decode(reader: TlReader): AdnlPing {
            val value = reader.readLong()
            return AdnlPing(value)
        }

        override fun encode(writer: TlWriter, value: AdnlPing) {
            writer.writeLong(value.value)
        }
    }
}
