package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("liteServer.blockLinkBack")
public data class LiteServerBlockLinkBack(
    @SerialName("to_key_block")
    @get:JvmName("toKeyBlock")
    override val toKeyBlock: Boolean,

    @get:JvmName("from")
    override val from: TonNodeBlockIdExt,

    @get:JvmName("to")
    override val to: TonNodeBlockIdExt,

    @SerialName("dest_proof")
    @get:JvmName("destProof")
    @Serializable(ByteStringBase64Serializer::class)
    val destProof: ByteString,

    @get:JvmName("proof")
    @Serializable(ByteStringBase64Serializer::class)
    val proof: ByteString,

    @SerialName("state_proof")
    @get:JvmName("stateProof")
    @Serializable(ByteStringBase64Serializer::class)
    val stateProof: ByteString
) : LiteServerBlockLink {

    public companion object : TlCodec<LiteServerBlockLinkBack> by LiteServerBlockLinkBackTlConstructor {
        @JvmStatic
        public fun tlConstructor(): TlConstructor<LiteServerBlockLinkBack> = LiteServerBlockLinkBackTlConstructor
    }
}

private object LiteServerBlockLinkBackTlConstructor : TlConstructor<LiteServerBlockLinkBack>(
    schema = "liteServer.blockLinkBack to_key_block:Bool from:tonNode.blockIdExt to:tonNode.blockIdExt dest_proof:bytes proof:bytes state_proof:bytes = liteServer.BlockLink"
) {
    override fun decode(reader: TlReader): LiteServerBlockLinkBack {
        val toKeyBlock = reader.readBoolean()
        val from = reader.read(TonNodeBlockIdExt)
        val to = reader.read(TonNodeBlockIdExt)
        val destProof = reader.readByteString()
        val proof = reader.readByteString()
        val stateProof = reader.readByteString()
        return LiteServerBlockLinkBack(toKeyBlock, from, to, destProof, proof, stateProof)
    }

    override fun encode(writer: TlWriter, value: LiteServerBlockLinkBack) {
        writer.writeBoolean(value.toKeyBlock)
        writer.write(TonNodeBlockIdExt, value.from)
        writer.write(TonNodeBlockIdExt, value.to)
        writer.writeBytes(value.destProof)
        writer.writeBytes(value.proof)
        writer.writeBytes(value.stateProof)
    }
}
