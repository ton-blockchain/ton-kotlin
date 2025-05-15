package org.ton.kotlin.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.lite.api.liteserver.LiteServerVersion
import org.ton.kotlin.tl.*

@Serializable
@SerialName("liteServer.getVersion")
public object LiteServerGetVersion :
    TLFunction<LiteServerGetVersion, LiteServerVersion>,
    TlCodec<LiteServerGetVersion> by LiteServerGetVersionTlConstructor {

    override fun tlCodec(): TlCodec<LiteServerGetVersion> = LiteServerGetVersionTlConstructor

    override fun resultTlCodec(): TlCodec<LiteServerVersion> = LiteServerVersion
}

private object LiteServerGetVersionTlConstructor : TlConstructor<LiteServerGetVersion>(
    schema = "liteServer.getVersion = liteServer.Version"
) {
    override fun decode(reader: TlReader): LiteServerGetVersion {
        return LiteServerGetVersion
    }

    override fun encode(writer: TlWriter, value: LiteServerGetVersion) {
    }
}
