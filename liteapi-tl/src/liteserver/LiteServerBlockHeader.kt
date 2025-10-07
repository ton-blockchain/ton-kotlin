package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.blockHeader")
public data class LiteServerBlockHeader(
    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @get:JvmName("mode")
    val mode: Int,

    @SerialName("header_proof")
    @get:JvmName("headerProof")
    @Serializable(ByteStringBase64Serializer::class)
    val headerProof: ByteString
) {
    public companion object : TlCodec<LiteServerBlockHeader> by LiteServerBlockHeaderTlConstructor
}

private object LiteServerBlockHeaderTlConstructor : TlConstructor<LiteServerBlockHeader>(
    "liteServer.blockHeader id:tonNode.blockIdExt mode:# header_proof:bytes = liteServer.BlockHeader"
) {
    override fun decode(reader: TlReader): LiteServerBlockHeader {
        val id = reader.read(TonNodeBlockIdExt)
        val mode = reader.readInt()
        val headerProof = reader.readByteString()
        return LiteServerBlockHeader(id, mode, headerProof)
    }

    override fun encode(writer: TlWriter, value: LiteServerBlockHeader) {
        writer.write(TonNodeBlockIdExt, value.id)
        writer.writeInt(value.mode)
        writer.writeBytes(value.headerProof)
    }
}
