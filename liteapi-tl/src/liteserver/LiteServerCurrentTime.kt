package org.ton.lite.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
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
