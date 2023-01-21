package org.ton.lite.api.liteserver

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@Serializable
@SerialName("liteServer.signatureSet")
public data class LiteServerSignatureSet(
    @SerialName("validator_set_hash")
    val validatorSetHash: Int,
    @SerialName("catchain_seqno")
    val catchainSeqno: Int,
    val signatures: List<LiteServerSignature>
) : Collection<LiteServerSignature> by signatures {
    public companion object : TlCodec<LiteServerSignatureSet> by LiteServerSignatureSetTlConstructor
}

private object LiteServerSignatureSetTlConstructor : TlConstructor<LiteServerSignatureSet>(
    schema = "liteServer.signatureSet validator_set_hash:int catchain_seqno:int signatures:(vector liteServer.signature) = liteServer.SignatureSet"
) {
    override fun decode(reader: TlReader): LiteServerSignatureSet {
        val validatorSetHash = reader.readInt()
        val catchainSeqno = reader.readInt()
        val signatures = reader.readVector {
            read(LiteServerSignature)
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
