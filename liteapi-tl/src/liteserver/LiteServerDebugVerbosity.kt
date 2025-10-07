package org.ton.lite.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.debug.verbosity")
public data class LiteServerDebugVerbosity(
    @get:JvmName("value")
    val value: Int
) {
    public companion object : TlCodec<LiteServerDebugVerbosity> by LiteServerDebugVerbosityTlConstructor
}

private object LiteServerDebugVerbosityTlConstructor : TlConstructor<LiteServerDebugVerbosity>(
    schema = "liteServer.debug.verbosity value:int = liteServer.debug.Verbosity"
) {
    override fun decode(reader: TlReader): LiteServerDebugVerbosity {
        val value = reader.readInt()
        return LiteServerDebugVerbosity(value)
    }

    override fun encode(writer: TlWriter, value: LiteServerDebugVerbosity) {
        writer.writeInt(value.value)
    }
}
