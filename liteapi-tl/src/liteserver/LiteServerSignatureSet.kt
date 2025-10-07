package org.ton.lite.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.signatureSet")
public data class LiteServerSignatureSet(
    @SerialName("validator_set_hash")
    @get:JvmName("validatorSetHash")
    val validatorSetHash: Int,

    @SerialName("catchain_seqno")
    @get:JvmName("catchainSeqno")
    val catchainSeqno: Int,

    @get:JvmName("signatures")
    val signatures: List<LiteServerSignature>
) {
    public companion object : TlCodec<LiteServerSignatureSet> by LiteServerSignatureSetTlConstructor
}

private object LiteServerSignatureSetTlConstructor : TlConstructor<LiteServerSignatureSet>(
    schema = "liteServer.signatureSet validator_set_hash:int catchain_seqno:int signatures:vector liteServer.signature = liteServer.SignatureSet"
) {
    override fun decode(reader: TlReader): LiteServerSignatureSet {
        val validatorSetHash = reader.readInt()
        val catchainSeqno = reader.readInt()
        val size = reader.readInt()
        val signatures = List(size) {
            reader.read(LiteServerSignature)
        }
        return LiteServerSignatureSet(validatorSetHash, catchainSeqno, signatures)
    }

    override fun encode(writer: TlWriter, value: LiteServerSignatureSet) {
        writer.writeInt(value.validatorSetHash)
        writer.writeInt(value.catchainSeqno)
        writer.writeVector(value.signatures) {
            write(LiteServerSignature, it)
        }
    }
}
