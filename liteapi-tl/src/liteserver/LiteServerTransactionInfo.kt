package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.transactionInfo")
public data class LiteServerTransactionInfo(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("proof")
    @Serializable(ByteStringBase64Serializer::class)
    val proof: ByteString,

    @get:JvmName("transaction")
    @Serializable(ByteStringBase64Serializer::class)
    val transaction: ByteString
) {
    public companion object : TlCodec<LiteServerTransactionInfo> by LiteServerTransactionInfoTlConstructor
}

private object LiteServerTransactionInfoTlConstructor : TlConstructor<LiteServerTransactionInfo>(
    schema = "liteServer.transactionInfo id:tonNode.blockIdExt proof:bytes transaction:bytes = liteServer.TransactionInfo"
) {
    override fun decode(reader: TlReader): LiteServerTransactionInfo {
        val id = reader.read(TonNodeBlockIdExt)
        val proof = reader.readByteString()
        val transaction = reader.readByteString()
        return LiteServerTransactionInfo(id, proof, transaction)
    }

    override fun encode(writer: TlWriter, value: LiteServerTransactionInfo) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeBytes(value.proof)
        writer.writeBytes(value.transaction)
    }
}
