package org.ton.api.adnl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmName

@Serializable
@SerialName("adnl.pong")
public data class AdnlPong(
    @get:JvmName("value")
    val value: Long
) {
    public companion object : TlConstructor<AdnlPong>(
        schema = "adnl.pong value:long = adnl.Pong"
    ) {
        override fun decode(reader: TlReader): AdnlPong {
            val value = reader.readLong()
            return AdnlPong(value)
        }

        override fun encode(output: TlWriter, value: AdnlPong) {
            output.writeLong(value.value)
        }
    }
}
