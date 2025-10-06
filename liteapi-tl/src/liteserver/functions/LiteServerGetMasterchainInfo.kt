package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.*
import org.ton.lite.api.liteserver.LiteServerMasterchainInfo

@Serializable
@SerialName("liteServer.getMasterchainInfo")
public object LiteServerGetMasterchainInfo :
    TLFunction<LiteServerGetMasterchainInfo, LiteServerMasterchainInfo>,
    TlCodec<LiteServerGetMasterchainInfo> by LiteServerGetMasterchainInfoTlConstructor {
    override fun tlCodec(): TlCodec<LiteServerGetMasterchainInfo> = LiteServerGetMasterchainInfoTlConstructor
    override fun resultTlCodec(): TlCodec<LiteServerMasterchainInfo> = LiteServerMasterchainInfo
}

private object LiteServerGetMasterchainInfoTlConstructor : TlConstructor<LiteServerGetMasterchainInfo>(
    schema = "liteServer.getMasterchainInfo = liteServer.MasterchainInfo"
) {
    override fun decode(reader: TlReader): LiteServerGetMasterchainInfo = LiteServerGetMasterchainInfo

    override fun encode(writer: TlWriter, value: LiteServerGetMasterchainInfo) {
    }
}
