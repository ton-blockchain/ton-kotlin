package org.ton.lite.api.liteserver.functions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter
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
