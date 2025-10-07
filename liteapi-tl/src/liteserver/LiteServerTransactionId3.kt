package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.transactionId3")
public data class LiteServerTransactionId3(
    @get:JvmName("account")
    @Serializable(ByteStringBase64Serializer::class)
    val account: ByteString,

    @get:JvmName("lt")
    val lt: Long
) {
    init {
        require(account.size == 32) { "account must be 32 bytes long" }
    }

    public companion object : TlCodec<LiteServerTransactionId3> by LiteServerTransactionId3TlConstructor
}

private object LiteServerTransactionId3TlConstructor : TlConstructor<LiteServerTransactionId3>(
    schema = "liteServer.transactionId3 account:int256 lt:long = liteServer.TransactionId3"
) {
    override fun decode(reader: TlReader): LiteServerTransactionId3 {
        val account = reader.readByteString(32)
        val lt = reader.readLong()
        return LiteServerTransactionId3(account, lt)
    }

    override fun encode(writer: TlWriter, value: LiteServerTransactionId3) {
        writer.writeRaw(value.account)
        writer.writeLong(value.lt)
    }
}
