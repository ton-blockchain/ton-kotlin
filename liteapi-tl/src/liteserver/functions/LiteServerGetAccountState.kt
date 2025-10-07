package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.lite.api.liteserver.LiteServerAccountId
import org.ton.lite.api.liteserver.LiteServerAccountState
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.getAccountState")
public data class LiteServerGetAccountState(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("account")
    val account: LiteServerAccountId,
) : TLFunction<LiteServerGetAccountState, LiteServerAccountState> {
    override fun tlCodec(): TlCodec<LiteServerGetAccountState> = LiteServerGetAccountStateTlConstructor

    override fun resultTlCodec(): TlCodec<LiteServerAccountState> = LiteServerAccountState

    public companion object : TlCodec<LiteServerGetAccountState> by LiteServerGetAccountStateTlConstructor
}

private object LiteServerGetAccountStateTlConstructor : TlConstructor<LiteServerGetAccountState>(
    schema = "liteServer.getAccountState id:tonNode.blockIdExt account:liteServer.accountId = liteServer.AccountState"
) {
    override fun decode(reader: TlReader): LiteServerGetAccountState {
        val id = reader.read(TonNodeBlockIdExt)
        val account = reader.read(LiteServerAccountId)
        return LiteServerGetAccountState(id, account)
    }

    override fun encode(writer: TlWriter, value: LiteServerGetAccountState) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.write(LiteServerAccountId, value.account)
    }
}
