package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.blockTransactions")
public data class LiteServerBlockTransactions(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @SerialName("req_count")
    @get:JvmName("reqCount")
    val reqCount: Int,

    @get:JvmName("incomplete")
    val incomplete: Boolean,

    @get:JvmName("ids")
    val ids: List<LiteServerTransactionId>,

    @get:JvmName("proof")
    @Serializable(ByteStringBase64Serializer::class)
    val proof: ByteString
) {
    public companion object : TlCodec<LiteServerBlockTransactions> by LiteServerBlockTransactionsTlConstructor
}

private object LiteServerBlockTransactionsTlConstructor : TlConstructor<LiteServerBlockTransactions>(
    schema = "liteServer.blockTransactions id:tonNode.blockIdExt req_count:# incomplete:Bool ids:(vector liteServer.transactionId) proof:bytes = liteServer.BlockTransactions"
) {
    override fun decode(reader: TlReader): LiteServerBlockTransactions {
        val id = reader.read(TonNodeBlockIdExt)
        val reqCount = reader.readInt()
        val incomplete = reader.readBoolean()
        val ids = List(reader.readInt()) {
            reader.read(LiteServerTransactionId)
        }
        val proof = reader.readByteString()
        return LiteServerBlockTransactions(id, reqCount, incomplete, ids, proof)
    }

    override fun encode(writer: TlWriter, value: LiteServerBlockTransactions) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeInt(value.reqCount)
        writer.writeBoolean(value.incomplete)
        writer.writeInt(value.ids.size)
        value.ids.forEach {
            writer.write(LiteServerTransactionId, it)
        }
    }
}
