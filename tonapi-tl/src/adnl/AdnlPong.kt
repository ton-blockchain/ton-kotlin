package org.ton.kotlin.api.adnl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter
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
