package org.ton.lite.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlCodec
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter

@Serializable
@SerialName("liteServer.queryPrefix")
public object LiteServerQueryPrefix : TlCodec<LiteServerQueryPrefix> by LiteServerQueryPrefixTlConstructor

private object LiteServerQueryPrefixTlConstructor : TlConstructor<LiteServerQueryPrefix>(
    schema = "liteServer.queryPrefix = Object"
) {
    override fun decode(reader: TlReader): LiteServerQueryPrefix = LiteServerQueryPrefix
    override fun encode(writer: TlWriter, value: LiteServerQueryPrefix) {}
}
