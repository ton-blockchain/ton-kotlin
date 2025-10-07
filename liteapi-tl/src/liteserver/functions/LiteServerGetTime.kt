package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.lite.api.liteserver.LiteServerCurrentTime
import org.ton.tl.*

@Serializable
@SerialName("liteServer.getTime")
public object LiteServerGetTime :
    TLFunction<LiteServerGetTime, LiteServerCurrentTime>,
    TlCodec<LiteServerGetTime> by LiteServerGetTimeTLConstructor {
    override fun tlCodec(): TlCodec<LiteServerGetTime> = LiteServerGetTimeTLConstructor
    override fun resultTlCodec(): TlCodec<LiteServerCurrentTime> = LiteServerCurrentTime
}

private object LiteServerGetTimeTLConstructor : TlConstructor<LiteServerGetTime>(
    schema = "liteServer.getTime = liteServer.CurrentTime"
) {
    override fun decode(reader: TlReader): LiteServerGetTime = LiteServerGetTime

    override fun encode(writer: TlWriter, value: LiteServerGetTime) {
    }
}
