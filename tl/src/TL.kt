@file:OptIn(ExperimentalSerializationApi::class, ExperimentalUnsignedTypes::class)

package org.ton.sdk.tl

import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.ton.sdk.tl.internal.SinkTlWriter
import org.ton.sdk.tl.internal.SourceTlReader
import org.ton.sdk.tl.internal.TlDecoderImpl
import org.ton.sdk.tl.internal.TlEncoderImpl

internal const val TL_BOOL_TRUE = 0x997275b5.toInt()
internal const val TL_BOOL_FALSE = 0xbc799737.toInt()

public open class TL(
    override val serializersModule: SerializersModule = EmptySerializersModule(),
    public val boxed: Boolean = true
) : BinaryFormat {
    override fun <T> encodeToByteArray(
        serializer: SerializationStrategy<T>,
        value: T
    ): ByteArray {
        val buffer = Buffer()
        encodeIntoSink(serializer, value, buffer)
        return buffer.readByteArray()
    }

    override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray
    ): T {
        val buffer = Buffer()
        buffer.write(bytes)
        return decodeFromSource(deserializer, buffer)
    }

    public fun <T> encodeToByteString(serializer: SerializationStrategy<T>, value: T): ByteString {
        val buffer = Buffer()
        encodeIntoSink(serializer, value, buffer)
        return buffer.readByteString()
    }

    public fun <T> decodeFromByteString(
        deserializer: DeserializationStrategy<T>,
        byteString: ByteString
    ): T {
        val buffer = Buffer()
        buffer.write(byteString)
        return decodeFromSource(deserializer, buffer)
    }

    public inline fun <reified T> decodeFromByteString(byteString: ByteString): T =
        decodeFromByteString(serializer(), byteString)

    public inline fun <reified T> encodeToByteString(value: T): ByteString = encodeToByteString(serializer(), value)

    public fun <T> decodeFromSource(
        deserializer: DeserializationStrategy<T>,
        source: Source
    ): T {
        val decoder = TlDecoderImpl(this, SourceTlReader(source))
        return decoder.decodeSerializableValue(deserializer)
    }

    public fun <T> encodeIntoSink(serializer: SerializationStrategy<T>, value: T, sink: Sink) {
        val encoder = TlEncoderImpl(this, SinkTlWriter(sink))
        encoder.encodeSerializableValue(serializer, value)
    }

    public companion object : TL()

    public object Boxed : TL(boxed = true)
}

public interface TlDecoder : Decoder {
    public val tl: TL

    public fun decodeByteArray(size: Int): ByteArray
}

public interface TlEncoder : Encoder {
    public val tl: TL
}
