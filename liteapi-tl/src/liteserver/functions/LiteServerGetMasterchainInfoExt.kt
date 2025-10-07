package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.lite.api.liteserver.LiteServerMasterchainInfoExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getMasterchainInfoExt")
public data class LiteServerGetMasterchainInfoExt(
    @get:JvmName("mode")
    val mode: Int
) : TLFunction<LiteServerGetMasterchainInfoExt, LiteServerMasterchainInfoExt> {
    public companion object : TlCodec<LiteServerGetMasterchainInfoExt> by LiteServerGetMasterchainInfoExtTlConstructor

    override fun tlCodec(): TlCodec<LiteServerGetMasterchainInfoExt> = LiteServerGetMasterchainInfoExtTlConstructor

    override fun resultTlCodec(): TlCodec<LiteServerMasterchainInfoExt> = LiteServerMasterchainInfoExt
}

private object LiteServerGetMasterchainInfoExtTlConstructor : TlConstructor<LiteServerGetMasterchainInfoExt>(
    schema = "liteServer.getMasterchainInfoExt mode:# = liteServer.MasterchainInfoExt"
) {
    override fun decode(reader: TlReader): LiteServerGetMasterchainInfoExt {
        val mode = reader.readInt()
        return LiteServerGetMasterchainInfoExt(mode)
    }

    override fun encode(writer: TlWriter, value: LiteServerGetMasterchainInfoExt) {
        writer.writeInt(value.mode)
    }
}
