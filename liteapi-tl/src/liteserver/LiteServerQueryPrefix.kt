package org.ton.lite.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

@Serializable
@SerialName("liteServer.queryPrefix")
public object LiteServerQueryPrefix : TlCodec<LiteServerQueryPrefix> by LiteServerQueryPrefixTlConstructor

private object LiteServerQueryPrefixTlConstructor : TlConstructor<LiteServerQueryPrefix>(
    schema = "liteServer.queryPrefix = Object"
) {
    override fun decode(reader: TlReader): LiteServerQueryPrefix = LiteServerQueryPrefix
    override fun encode(writer: TlWriter, value: LiteServerQueryPrefix) {}
}
