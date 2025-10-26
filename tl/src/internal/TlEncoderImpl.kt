package org.ton.sdk.tl.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import org.ton.sdk.tl.*

internal open class TlEncoderImpl(
    override val tl: TL,
    val writer: TlWriter,
) : TlEncoder {
    override val serializersModule: SerializersModule get() = tl.serializersModule

    @ExperimentalSerializationApi
    override fun encodeNull() {
    }

    override fun encodeBoolean(value: Boolean) = writer.writeBoolean(value)
    override fun encodeByte(value: Byte) = writer.writeByte(value)
    override fun encodeChar(value: Char) = writer.writeChar(value)
    override fun encodeShort(value: Short) = writer.writeShort(value)
    override fun encodeInt(value: Int) = writer.writeInt(value)
    override fun encodeLong(value: Long) = writer.writeLong(value)
    override fun encodeFloat(value: Float) = writer.writeFloat(value)
    override fun encodeDouble(value: Double) = writer.writeDouble(value)
    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this
    override fun encodeString(value: String) = writer.writeString(value)

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return TlCompositeEncoder(tl, writer)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        TODO()
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        writer.writeInt(collectionSize)
        return beginStructure(descriptor)
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        if (serializer !is AbstractPolymorphicSerializer<T>) {
            if (tl.boxed) {
                encodeInt(serializer.descriptor.getTlConstructorId())
            }
            return serializer.serialize(this, value)
        }
        val actualSerializer = serializer.findPolymorphicSerializerOrNull(this, value)
            ?: error("No polymorphic serializer found for value: $value")
        val constructorId = actualSerializer.descriptor.getTlConstructorId()
        encodeInt(constructorId)
        actualSerializer.serialize(this, value)
    }

    open class TlCompositeEncoder(
        tl: TL,
        writer: TlWriter,
        var fixedSize: Int = -1,
    ) : TlEncoderImpl(tl, writer), CompositeEncoder {
        override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            return TlCompositeEncoder(tl, writer, fixedSize)
        }

        override fun endStructure(descriptor: SerialDescriptor) {
        }

        override fun encodeBooleanElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Boolean
        ) = encodeBoolean(value)

        override fun encodeByteElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Byte
        ) = encodeByte(value)

        override fun encodeShortElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Short
        ) = encodeShort(value)

        override fun encodeCharElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Char
        ) = encodeChar(value)

        override fun encodeIntElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Int
        ) = encodeInt(value)

        override fun encodeLongElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Long
        ) = encodeLong(value)

        override fun encodeFloatElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Float
        ) = encodeFloat(value)

        override fun encodeDoubleElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: Double
        ) = encodeDouble(value)

        override fun encodeStringElement(
            descriptor: SerialDescriptor,
            index: Int,
            value: String
        ) = encodeString(value)

        override fun encodeInlineElement(
            descriptor: SerialDescriptor,
            index: Int
        ): Encoder = encodeInline(descriptor)

        @OptIn(InternalSerializationApi::class)
        override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
            if (serializer.descriptor == ByteArraySerializer().descriptor) {
                return if (fixedSize > 0) {
                    writer.writeByteArray(value as ByteArray, 0, fixedSize)
                    fixedSize = -1 // Reset fixedSize after writing
                } else {
                    writer.writeByteArray(value as ByteArray)
                }
            }
            if (serializer !is AbstractPolymorphicSerializer<T>) {
                return serializer.serialize(this, value)
            }
            val actualSerializer = serializer.findPolymorphicSerializerOrNull(this, value)
                ?: error("No polymorphic serializer found for value: $value")
            val constructorId = actualSerializer.descriptor.getTlConstructorId()
            encodeInt(constructorId)
            actualSerializer.serialize(this, value)
        }

        @OptIn(InternalSerializationApi::class)
        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            try {
                for (annotation in descriptor.getElementAnnotations(index)) {
                    if (annotation is Bits128) {
                        fixedSize = 16
                        break
                    } else if (annotation is Bits256) {
                        fixedSize = 32
                        break
                    } else if (annotation is TlFixedSize) {
                        fixedSize = annotation.value
                        break
                    }
                }
                encodeSerializableValue(serializer, value)
            } finally {
                fixedSize = -1
            }
        }

        @ExperimentalSerializationApi
        override fun <T : Any> encodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T?
        ) = encodeNullableSerializableValue(serializer, value)
    }
}
