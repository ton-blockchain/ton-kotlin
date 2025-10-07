package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.api.liteserver.LiteServerTransactionInfo
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getOneTransaction")
public data class LiteServerGetOneTransaction(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("account")
    val account: LiteServerAccountId,

    @get:JvmName("lt")
    val lt: Long
) : TLFunction<LiteServerGetOneTransaction, LiteServerTransactionInfo> {
    override fun tlCodec(): TlCodec<LiteServerGetOneTransaction> = LiteServerGetOneTransaction
    override fun resultTlCodec(): TlCodec<LiteServerTransactionInfo> = LiteServerTransactionInfo

    public companion object : TlCodec<LiteServerGetOneTransaction> by LiteServerGetOneTransactionTlConstructor
}

private object LiteServerGetOneTransactionTlConstructor : TlConstructor<LiteServerGetOneTransaction>(
    schema = "liteServer.getOneTransaction id:tonNode.blockIdExt account:liteServer.accountId lt:long = liteServer.TransactionInfo"
) {
    override fun decode(reader: TlReader): LiteServerGetOneTransaction {
        val id = reader.read(TonNodeBlockIdExt)
        val account = reader.read(LiteServerAccountId)
        val lt = reader.readLong()
        return LiteServerGetOneTransaction(id, account, lt)
    }

    override fun encode(output: TlWriter, value: LiteServerGetOneTransaction) {
        output.write(TonNodeBlockIdExt, value.id)
        output.write(LiteServerAccountId, value.account)
        output.writeLong(value.lt)
    }
}
