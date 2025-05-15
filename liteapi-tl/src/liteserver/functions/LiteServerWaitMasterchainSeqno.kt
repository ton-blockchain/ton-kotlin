package org.ton.kotlin.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.TlCodec
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.waitMasterchainSeqno")
public data class LiteServerWaitMasterchainSeqno(
    @get:JvmName("seqno")
    val seqno: Int,

    @SerialName("timeout_ms")
    @get:JvmName("timeoutMs")
    val timeoutMs: Int
) {
    public companion object : TlCodec<LiteServerWaitMasterchainSeqno> by LiteServerWaitMasterchainSeqnoTlConstructor
}

private object LiteServerWaitMasterchainSeqnoTlConstructor : TlConstructor<LiteServerWaitMasterchainSeqno>(
    schema = "liteServer.waitMasterchainSeqno seqno:int timeout_ms:int = Object"
) {
    override fun decode(reader: TlReader): LiteServerWaitMasterchainSeqno {
        val seqno = reader.readInt()
        val timeoutMs = reader.readInt()
        return LiteServerWaitMasterchainSeqno(seqno, timeoutMs)
    }

    override fun encode(writer: TlWriter, value: LiteServerWaitMasterchainSeqno) {
        writer.writeInt(value.seqno)
        writer.writeInt(value.timeoutMs)
    }
}
