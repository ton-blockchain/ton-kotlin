@file:OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package org.ton.kotlin.tl

import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteArray
import kotlinx.io.readByteString
import kotlinx.io.write
import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.ton.kotlin.tl.internal.SinkTlWriter
import org.ton.kotlin.tl.internal.SourceTlReader
import org.ton.kotlin.tl.internal.TlDecoderImpl
import org.ton.kotlin.tl.internal.TlEncoderImpl

internal const val TL_BOOL_TRUE = 0x997275b5.toInt()
internal const val TL_BOOL_FALSE = 0xbc799737.toInt()

open class TL(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    val boxed: Boolean = true
) : BinaryFormat {
    override fun <T> encodeToByteArray(
        serializer: SerializationStrategy<T>,
        value: T
    ): ByteArray {
        val buffer = Buffer()
        val encoder = TlEncoderImpl(this, SinkTlWriter(buffer))
        encoder.encodeSerializableValue(serializer, value)
        return buffer.readByteArray()
    }

    override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray
    ): T {
        val buffer = Buffer()
        buffer.write(bytes)
        val decoder = TlDecoderImpl(this, SourceTlReader(buffer))
        return decoder.decodeSerializableValue(deserializer)
    }

    fun <T> encodeToByteString(serializer: SerializationStrategy<T>, value: T): ByteString {
        val buffer = Buffer()
        val encoder = TlEncoderImpl(this, SinkTlWriter(buffer))
        encoder.encodeSerializableValue(serializer, value)
        return buffer.readByteString()
    }

    fun <T> decodeFromByteString(
        deserializer: DeserializationStrategy<T>,
        byteString: ByteString
    ): T {
        val buffer = Buffer()
        buffer.write(byteString)
        val decoder = TlDecoderImpl(this, SourceTlReader(buffer))
        return decoder.decodeSerializableValue(deserializer)
    }

    inline fun <reified T> decodeFromByteString(byteString: ByteString): T =
        decodeFromByteString(serializer(), byteString)

    inline fun <reified T> encodeToByteString(value: T): ByteString = encodeToByteString(serializer(), value)

    companion object : TL()

    object Boxed : TL(boxed = true)
}

interface TlDecoder : Decoder {
    val tl: TL
}

interface TlEncoder : Encoder {
    val tl: TL
}
