package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.api.tonnode.TonNodeZeroStateIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.masterchainInfoExt")
public data class LiteServerMasterchainInfoExt(
    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("version")
    val version: Int,

    @get:JvmName("capabilities")
    val capabilities: Long,

    @get:JvmName("last")
    val last: TonNodeBlockIdExt,

    @SerialName("last_utime")
    @get:JvmName("lastUTime")
    val lastUTime: Int,

    @get:JvmName("name")
    val now: Int,

    @SerialName("state_root_hash")
    @get:JvmName("stateRootHash")
    @Serializable(ByteStringBase64Serializer::class)
    val stateRootHash: ByteString,

    @get:JvmName("init")
    val init: TonNodeZeroStateIdExt
) {
    public companion object : TlConstructor<LiteServerMasterchainInfoExt>(
        schema = "liteServer.masterchainInfoExt mode:# version:int capabilities:long last:tonNode.blockIdExt last_utime:int now:int state_root_hash:int256 init:tonNode.zeroStateIdExt = liteServer.MasterchainInfoExt"
    ) {
        override fun decode(reader: TlReader): LiteServerMasterchainInfoExt {
            val mode = reader.readInt()
            val version = reader.readInt()
            val capabilities = reader.readLong()
            val last = reader.read(TonNodeBlockIdExt)
            val lastUtime = reader.readInt()
            val now = reader.readInt()
            val stateRootHash = reader.readByteString(32)
            val init = reader.read(TonNodeZeroStateIdExt)
            return LiteServerMasterchainInfoExt(
                mode,
                version,
                capabilities,
                last,
                lastUtime,
                now,
                stateRootHash,
                init
            )
        }

        override fun encode(writer: TlWriter, value: LiteServerMasterchainInfoExt) {
            writer.writeInt(value.mode)
            writer.writeInt(value.version)
            writer.writeLong(value.capabilities)
            writer.write(TonNodeBlockIdExt, value.last)
            writer.writeInt(value.lastUTime)
            writer.writeInt(value.now)
            writer.writeRaw(value.stateRootHash)
            writer.write(TonNodeZeroStateIdExt, value.init)
        }
    }
}
