@file:Suppress("OPT_IN_USAGE")

package org.ton.api.dht.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmName

@SerialName("dht.config.random.local")
@Polymorphic
@Serializable
@JsonClassDiscriminator("@type")
public class DhtConfigRandomLocal(
    @get:JvmName("cnt")
    public val cnt: Int
) : DhtConfigLocal {
    public companion object : TlConstructor<DhtConfigRandomLocal>(
        schema = "dht.config.random.local cnt:int = dht.config.Local",
    ) {
        override fun encode(writer: TlWriter, value: DhtConfigRandomLocal) {
            writer.writeInt(value.cnt)
        }

        override fun decode(reader: TlReader): DhtConfigRandomLocal {
            val cnt = reader.readInt()
            return DhtConfigRandomLocal(cnt)
        }
    }
}
