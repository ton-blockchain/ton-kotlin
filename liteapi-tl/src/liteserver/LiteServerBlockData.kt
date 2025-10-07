package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.blockData")
public class LiteServerBlockData(
    @get:JvmName("id")
    public val id: TonNodeBlockIdExt,

    @get:JvmName("data")
    @Serializable(ByteStringBase64Serializer::class)
    public val data: ByteString
) {
    public companion object : TlConstructor<LiteServerBlockData>(
        schema = "liteServer.blockData id:tonNode.blockIdExt data:bytes = liteServer.BlockData"
    ) {
        override fun decode(reader: TlReader): LiteServerBlockData {
            val id = reader.read(TonNodeBlockIdExt)
            val data = reader.readByteString()
            return LiteServerBlockData(id, data)
        }

        override fun encode(writer: TlWriter, value: LiteServerBlockData) {
            writer.write(TonNodeBlockIdExt, value.id)
            writer.writeBytes(value.data)
        }
    }
}
