package org.ton.api.adnl.message

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*
import org.ton.tl.constructors.BytesTlConstructor
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

@SerialName("adnl.message.part")
@Serializable
public data class AdnlMessagePart(
    @get:JvmName("hash")
    @Serializable(ByteStringBase64Serializer::class)
    val hash: ByteString,

    @SerialName("total_size")
    @get:JvmName("totalSize")
    val totalSize: Int,

    @get:JvmName("offset")
    val offset: Int,

    @get:JvmName("data")
    @Serializable(ByteStringBase64Serializer::class)
    val data: ByteString
) : AdnlMessage {
    init {
        check(hash.size == 32) { "hash size expected: 32, actual: ${hash.size}" }
    }

    public companion object : TlCodec<AdnlMessagePart> by AdnlMessagePartTlConstructor {
        @JvmStatic
        public fun tlConstructor(): TlConstructor<AdnlMessagePart> = AdnlMessagePartTlConstructor

        @JvmStatic
        public fun sizeOf(value: AdnlMessagePart): Int =
            256 / 8 +
                    Int.SIZE_BYTES +
                    Int.SIZE_BYTES +
                    BytesTlConstructor.sizeOf(value.data)

        @JvmStatic
        public fun build(message: AdnlMessage, maxSize: Int): List<AdnlMessagePart> {
            val data = message.toByteArray()
            val hash = message.hash()
            var offset = 0
            val parts = ArrayList<AdnlMessagePart>()
            while (offset < data.size) {
                val partSize = minOf(maxSize, data.size - offset)
                val part = AdnlMessagePart(
                    hash = ByteString(*hash),
                    totalSize = data.size,
                    offset = offset,
                    data = ByteString(*data.copyOfRange(offset, offset + partSize))
                )
                parts.add(part)
                offset += partSize
            }
            return parts
        }
    }
}

private object AdnlMessagePartTlConstructor : TlConstructor<AdnlMessagePart>(
    schema = "adnl.message.part hash:int256 total_size:int offset:int data:bytes = adnl.Message;",
) {
    override fun decode(reader: TlReader): AdnlMessagePart {
        val hash = reader.readByteString(32)
        val totalSize = reader.readInt()
        val offset = reader.readInt()
        val data = reader.readByteString()
        return AdnlMessagePart(hash, totalSize, offset, data)
    }

    override fun encode(writer: TlWriter, value: AdnlMessagePart) {
        writer.writeRaw(value.hash)
        writer.writeInt(value.totalSize)
        writer.writeInt(value.offset)
        writer.writeBytes(value.data)
    }
}
