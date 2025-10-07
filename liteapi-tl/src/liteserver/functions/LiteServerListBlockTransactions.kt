package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerBlockTransactions
import org.ton.lite.api.liteserver.LiteServerTransactionId3
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.listBlockTransactions")
public data class LiteServerListBlockTransactions(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("count")
    val count: Int,

    @get:JvmName("after")
    val after: LiteServerTransactionId3?,

    @SerialName("reverse_order")
    @get:JvmName("reverseOrder")
    val reverseOrder: Boolean = true,

    @SerialName("want_proof")
    @get:JvmName("wantProof")
    val wantProof: Boolean = true
) : TLFunction<LiteServerListBlockTransactions, LiteServerBlockTransactions> {
    public companion object : TlCodec<LiteServerListBlockTransactions> by LiteServerListBlockTransactionsTlConstructor

    override fun tlCodec(): TlCodec<LiteServerListBlockTransactions> = LiteServerListBlockTransactions
    override fun resultTlCodec(): TlCodec<LiteServerBlockTransactions> = LiteServerBlockTransactions
}

private object LiteServerListBlockTransactionsTlConstructor : TlConstructor<LiteServerListBlockTransactions>(
    schema = "liteServer.listBlockTransactions id:tonNode.blockIdExt mode:# count:# after:mode.7?liteServer.transactionId3 reverse_order:mode.6?true want_proof:mode.5?true = liteServer.BlockTransactions"
) {
    override fun decode(reader: TlReader): LiteServerListBlockTransactions {
        val id = reader.read(TonNodeBlockIdExt)
        val mode = reader.readInt()
        val count = reader.readInt()
        val after = reader.readNullable(mode, 7) {
            read(LiteServerTransactionId3)
        }
        val reverseOrder = reader.readNullable(mode, 6) { true } ?: false
        val wantProof = reader.readNullable(mode, 5) { true } ?: false
        return LiteServerListBlockTransactions(id, mode, count, after, reverseOrder, wantProof)
    }

    override fun encode(writer: TlWriter, value: LiteServerListBlockTransactions) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeInt(value.mode)
        writer.writeInt(value.count)
        writer.writeNullable(value.mode, 7, value.after) {
            write(LiteServerTransactionId3, it)
        }
        writer.writeNullable(value.mode, 6, value.reverseOrder) {
        }
        writer.writeNullable(value.mode, 5, value.wantProof) {
        }
    }
}
