package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("liteServer.transactionId")
public data class LiteServerTransactionId internal constructor(
    val mode: Int,
    @Serializable(ByteStringBase64Serializer::class)
    val account: ByteString?,
    val lt: Long?,
    @Serializable(ByteStringBase64Serializer::class)
    val hash: ByteString?
) {
    init {
        require(account == null || account.size == 32) { "account must be 32 bytes long" }
        require(hash == null || hash.size == 32) { "hash must be 32 bytes long" }
    }

    public companion object : TlCodec<LiteServerTransactionId> by LiteServerTransactionIdTlConstructor {
        @JvmStatic
        public fun mode(account: Boolean, lt: Boolean, hash: Boolean): Int {
            var mode = 0
            if (account) mode = mode or 1
            if (lt) mode = mode or 2
            if (hash) mode = mode or 4
            return mode
        }
    }
}

private object LiteServerTransactionIdTlConstructor : TlConstructor<LiteServerTransactionId>(
    schema = "liteServer.transactionId mode:# account:mode.0?int256 lt:mode.1?long hash:mode.2?int256 = liteServer.TransactionId"
) {
    override fun decode(reader: TlReader): LiteServerTransactionId {
        val mode = reader.readInt()
        val account = reader.readNullable(mode, 0) {
            readByteString(32)
        }
        val lt = reader.readNullable(mode, 1) {
            readLong()
        }
        val hash = reader.readNullable(mode, 2) {
            readByteString(32)
        }
        return LiteServerTransactionId(mode, account, lt, hash)
    }

    override fun encode(writer: TlWriter, value: LiteServerTransactionId) {
        writer.writeInt(value.mode)
        val mode = value.mode
        writer.writeNullable(mode, 0, value.account) {
            writeRaw(it)
        }
        writer.writeNullable(mode, 1, value.lt) {
            writeLong(it)
        }
        writer.writeNullable(mode, 2, value.hash) {
            writeRaw(it)
        }
    }
}
