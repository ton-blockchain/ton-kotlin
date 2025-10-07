package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.shardInfo")
public data class LiteServerShardInfo(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @SerialName("shardblk")
    @get:JvmName("shardBlock")
    val shardBlock: TonNodeBlockIdExt,

    @SerialName("shard_proof")
    @get:JvmName("shardProof")
    @Serializable(ByteStringBase64Serializer::class)
    val shardProof: ByteString,

    @SerialName("shard_descr")
    @get:JvmName("shardDescr")
    @Serializable(ByteStringBase64Serializer::class)
    val shardDescr: ByteString
) {
    public companion object : TlCodec<LiteServerShardInfo> by LiteServerShardInfoTlConstructor
}

private object LiteServerShardInfoTlConstructor : TlConstructor<LiteServerShardInfo>(
    schema = "liteServer.shardInfo id:tonNode.blockIdExt shardblk:tonNode.blockIdExt shard_proof:bytes shard_descr:bytes = liteServer.ShardInfo"
) {
    override fun decode(reader: TlReader): LiteServerShardInfo {
        val id = reader.read(TonNodeBlockIdExt)
        val shardblk = reader.read(TonNodeBlockIdExt)
        val shardProof = reader.readByteString()
        val shardDescr = reader.readByteString()
        return LiteServerShardInfo(id, shardblk, shardProof, shardDescr)
    }

    override fun encode(writer: TlWriter, value: LiteServerShardInfo) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.write(TonNodeBlockIdExt, value.shardBlock)
        writer.writeBytes(value.shardProof)
        writer.writeBytes(value.shardDescr)
    }
}
