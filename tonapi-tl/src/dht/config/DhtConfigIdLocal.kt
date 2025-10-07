@file:Suppress("OPT_IN_USAGE")

package org.ton.api.dht.config

import kotlinx.serialization.SerialName
import org.ton.api.adnl.AdnlIdShort
import org.ton.tl.*
import kotlin.jvm.JvmName

@SerialName("dht.config.local")
public data class DhtConfigIdLocal(
    @get:JvmName("id")
    val id: AdnlIdShort
) : DhtConfigLocal {
    public companion object : TlConstructor<DhtConfigIdLocal>(
        schema = "dht.config.local id:adnl.id_short = dht.config.Local"
    ) {
        override fun encode(writer: TlWriter, value: DhtConfigIdLocal) {
            writer.write(AdnlIdShort, value.id)
        }

        override fun decode(reader: TlReader): DhtConfigIdLocal {
            val id = reader.read(AdnlIdShort)
            return DhtConfigIdLocal(id)
        }
    }
}
