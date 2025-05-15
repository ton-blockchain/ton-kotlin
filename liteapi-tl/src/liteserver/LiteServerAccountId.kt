@file:UseSerializers(HexByteArraySerializer::class)

package org.ton.kotlin.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.HexByteArraySerializer
import org.ton.kotlin.tl.ByteStringBase64Serializer
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter
import kotlin.jvm.JvmName

@Serializable
@SerialName("liteServer.accountId")
public data class LiteServerAccountId(
    @get:JvmName("workchain")
    val workchain: Int,

    @get:JvmName("id")
    @Serializable(ByteStringBase64Serializer::class)
    val id: ByteString
) {
    public companion object : TlConstructor<LiteServerAccountId>(
        schema = "liteServer.accountId workchain:int id:int256 = liteServer.AccountId"
    ) {
        override fun decode(reader: TlReader): LiteServerAccountId {
            val workchain = reader.readInt()
            val id = reader.readByteString(32)
            return LiteServerAccountId(workchain, id)
        }

        override fun encode(writer: TlWriter, value: LiteServerAccountId) {
            writer.writeInt(value.workchain)
            writer.writeRaw(value.id)
        }
    }
}
