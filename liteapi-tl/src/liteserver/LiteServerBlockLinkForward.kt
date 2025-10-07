package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("liteServer.blockLinkForward")
public data class LiteServerBlockLinkForward(
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

    @SerialName("config_proof")
    @get:JvmName("configProof")
    @Serializable(ByteStringBase64Serializer::class)
    val configProof: ByteString,

    @get:JvmName("signatures")
    val signatures: LiteServerSignatureSet
) : LiteServerBlockLink {

    public companion object : TlCodec<LiteServerBlockLinkForward> by LiteServerBlockLinkForwardTlConstructor {
        @JvmStatic
        public fun tlConstructor(): TlConstructor<LiteServerBlockLinkForward> = LiteServerBlockLinkForwardTlConstructor
    }
}

private object LiteServerBlockLinkForwardTlConstructor : TlConstructor<LiteServerBlockLinkForward>(
    schema = "liteServer.blockLinkForward to_key_block:Bool from:tonNode.blockIdExt to:tonNode.blockIdExt dest_proof:bytes config_proof:bytes signatures:liteServer.SignatureSet = liteServer.BlockLink"
) {
    override fun decode(reader: TlReader): LiteServerBlockLinkForward {
        val toKeyBlock = reader.readBoolean()
        val from = reader.read(TonNodeBlockIdExt)
        val to = reader.read(TonNodeBlockIdExt)
        val destProof = reader.readByteString()
        val configProof = reader.readByteString()
        val signatures = LiteServerSignatureSet.decodeBoxed(reader)
        return LiteServerBlockLinkForward(toKeyBlock, from, to, destProof, configProof, signatures)
    }

    override fun encode(writer: TlWriter, value: LiteServerBlockLinkForward) {
        writer.writeBoolean(value.toKeyBlock)
        writer.write(TonNodeBlockIdExt, value.from)
        writer.write(TonNodeBlockIdExt, value.to)
        writer.writeBytes(value.destProof)
        writer.writeBytes(value.configProof)
        LiteServerSignatureSet.encodeBoxed(writer, value.signatures)
    }
}
