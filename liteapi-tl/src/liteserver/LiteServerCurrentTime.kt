package org.ton.kotlin.lite.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.currentTime")
public data class LiteServerCurrentTime(
    @get:JvmName("now")
    val now: Int
) {
    public companion object : TlConstructor<LiteServerCurrentTime>(
        schema = "liteServer.currentTime now:int = liteServer.CurrentTime"
    ) {
        override fun decode(reader: TlReader): LiteServerCurrentTime {
            val now = reader.readInt()
            return LiteServerCurrentTime(now)
        }

        override fun encode(writer: TlWriter, value: LiteServerCurrentTime) {
            writer.writeInt(value.now)
        }
    }
}
