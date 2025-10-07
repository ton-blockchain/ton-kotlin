@file:Suppress("PropertyName", "NOTHING_TO_INLINE")

package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.api.tonnode.TonNodeZeroStateIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.masterchainInfo")
public data class LiteServerMasterchainInfo(
    @get:JvmName("last")
    public val last: TonNodeBlockIdExt,

    @get:JvmName("stateRootHash")
    @Serializable(ByteStringBase64Serializer::class)
    public val stateRootHash: ByteString,

    @get:JvmName("init")
    public val init: TonNodeZeroStateIdExt
) {
    init {
        require(stateRootHash.size == 32) { "Invalid stateRootHash size: ${stateRootHash.size}, expected: 32" }
    }

    public companion object : TlCodec<LiteServerMasterchainInfo> by LiteServerMasterchainInfoTlbConstructor
}

private object LiteServerMasterchainInfoTlbConstructor : TlConstructor<LiteServerMasterchainInfo>(
    schema = "liteServer.masterchainInfo last:tonNode.blockIdExt state_root_hash:int256 init:tonNode.zeroStateIdExt = liteServer.MasterchainInfo"
) {
    override fun decode(reader: TlReader): LiteServerMasterchainInfo {
        val last = reader.read(TonNodeBlockIdExt)
        val stateRootHash = reader.readByteString(32)
        val init = reader.read(TonNodeZeroStateIdExt)
        return LiteServerMasterchainInfo(last, stateRootHash, init)
    }

    override fun encode(writer: TlWriter, value: LiteServerMasterchainInfo) {
        writer.write(TonNodeBlockIdExt, value.last)
        writer.writeRaw(value.stateRootHash)
        writer.write(TonNodeZeroStateIdExt, value.init)
    }
}
