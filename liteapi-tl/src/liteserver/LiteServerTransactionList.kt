package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.transactionList")
public data class LiteServerTransactionList(
    @get:JvmName("ids")
    val ids: List<TonNodeBlockIdExt>,

    @get:JvmName("transactions")
    @Serializable(ByteStringBase64Serializer::class)
    val transactions: ByteString
) {
    public companion object : TlCodec<LiteServerTransactionList> by LiteServerTransactionListTlConstructor
}

private object LiteServerTransactionListTlConstructor : TlConstructor<LiteServerTransactionList>(
    schema = "liteServer.transactionList ids:(vector tonNode.blockIdExt) transactions:bytes = liteServer.TransactionList",
) {
    override fun decode(reader: TlReader): LiteServerTransactionList {
        val ids = reader.readVector {
            read(TonNodeBlockIdExt)
        }
        val transactions = reader.readByteString()
        return LiteServerTransactionList(ids, transactions)
    }

    override fun encode(writer: TlWriter, value: LiteServerTransactionList) {
        writer.writeVector(value.ids) {
            write(TonNodeBlockIdExt, it)
        }
        writer.writeBytes(value.transactions)
    }
}
