package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerShardInfo
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getShardInfo")
public data class LiteServerGetShardInfo(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("workchain")
    val workchain: Int,

    @get:JvmName("shard")
    val shard: Long,

    @get:JvmName("exact")
    val exact: Boolean
) : TLFunction<LiteServerGetShardInfo, LiteServerShardInfo> {
    override fun tlCodec(): TlCodec<LiteServerGetShardInfo> = LiteServerGetShardInfo

    override fun resultTlCodec(): TlCodec<LiteServerShardInfo> = LiteServerShardInfo

    public companion object : TlCodec<LiteServerGetShardInfo> by LiteServerGetShardInfoTlConstructor
}

private object LiteServerGetShardInfoTlConstructor : TlConstructor<LiteServerGetShardInfo>(
    schema = "liteServer.getShardInfo id:tonNode.blockIdExt workchain:int shard:long exact:Bool = liteServer.ShardInfo"
) {
    override fun decode(input: TlReader): LiteServerGetShardInfo {
        val id = input.read(TonNodeBlockIdExt)
        val workchain = input.readInt()
        val shard = input.readLong()
        val exact = input.readBoolean()
        return LiteServerGetShardInfo(id, workchain, shard, exact)
    }

    override fun encode(output: TlWriter, value: LiteServerGetShardInfo) {
        output.write(TonNodeBlockIdExt, value.id)
        output.writeInt(value.workchain)
        output.writeLong(value.shard)
        output.writeBoolean(value.exact)
    }
}
