package org.ton.sdk.tl.internal

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import org.ton.sdk.tl.*


internal open class TlDecoderImpl(
    override val tl: TL,
    val reader: TlReader
) : TlDecoder {
    override val serializersModule: SerializersModule get() = tl.serializersModule

    @ExperimentalSerializationApi
    override fun decodeNotNullMark(): Boolean = true

    @ExperimentalSerializationApi
    override fun decodeNull(): Nothing? = null

    override fun decodeBoolean(): Boolean = reader.readBoolean()
    override fun decodeByte(): Byte = reader.readByte()
    override fun decodeShort(): Short = reader.readShort()
    override fun decodeChar(): Char = reader.readChar()
    override fun decodeInt(): Int = reader.readInt()
    override fun decodeLong(): Long = reader.readLong()
    override fun decodeFloat(): Float = reader.readFloat()
    override fun decodeDouble(): Double = reader.readDouble()
    override fun decodeString(): String = reader.readString()
    override fun decodeByteArray(size: Int): ByteArray = reader.readByteArray(size)


    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        TODO("Not yet implemented")
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        if (tl.boxed && deserializer !is AbstractPolymorphicSerializer<*>) {
            val constructorId = decodeInt()
            val annotations = deserializer.descriptor.annotations
            for (i in annotations.indices) {
                val annotation = annotations[i]
                if (annotation is TlConstructorId) {
                    if (annotation.id.toInt() != constructorId) {
                        throw SerializationException("Unexpected constructor id 0x${constructorId.toHexString()} ($constructorId) for ${deserializer.descriptor.serialName}")
                    }
                    break
                }
            }
        }
        if (deserializer !is AbstractPolymorphicSerializer<*>) {
            return super.decodeSerializableValue(deserializer)
        }
        val constructorId = decodeInt()
        val constructor2name = deserializer.constructorIdToSerialName(tl)
        val typeName = constructor2name[constructorId]
            ?: error("Unknown constructor id 0x${constructorId.toHexString()}($constructorId) for ${deserializer.descriptor.serialName}")

        @Suppress("UNCHECKED_CAST")
        val decoder = TlStructureDecoder(tl, reader, deserializer.descriptor)
        val actualSerializer = deserializer.findPolymorphicSerializer(decoder, typeName)
                as DeserializationStrategy<T>
        return actualSerializer.deserialize(decoder)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return TlStructureDecoder(tl, reader, descriptor)
    }
}

internal class TlStructureDecoder(
    tl: TL,
    reader: TlReader,
    val descriptor: SerialDescriptor,
    var exactSize: Int = -1
) : TlDecoderImpl(tl, reader), CompositeDecoder {
    private val intValues = Array(descriptor.elementsCount) { 0 }

    private var index = 0

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return TlStructureDecoder(tl, reader, descriptor, exactSize)
    }

    override fun endStructure(descriptor: SerialDescriptor) {
    }

    @ExperimentalSerializationApi
    override fun decodeSequentially(): Boolean {
        return true // Sequential decoding is assumed for TL structures
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = reader.readInt()

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        val elementIndex = index++
        if (elementIndex >= descriptor.elementsCount) {
            return CompositeDecoder.DECODE_DONE
        }
        return elementIndex
    }

    override fun decodeBooleanElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Boolean {
        val value = reader.readBoolean()
        if (value) {
            intValues[index] = 1.inv()
        } else {
            intValues[index] = 0
        }
        return value
    }

    override fun decodeByteElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Byte {
        val value = reader.readByte()
        intValues[index] = value.toInt()
        return value
    }

    override fun decodeCharElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Char {
        val value = reader.readChar()
        intValues[index] = value.code
        return value
    }

    override fun decodeShortElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Short {
        val value = reader.readShort()
        intValues[index] = value.toInt()
        return value
    }

    override fun decodeIntElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Int {
        val value = reader.readInt()
        intValues[index] = value
        return value
    }

    override fun decodeLongElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Long {
        val value = reader.readLong()
        intValues[index] = value.toInt() // Assuming we want to store the int representation
        return value
    }

    override fun decodeFloatElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Float = decodeFloat()

    override fun decodeDoubleElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Double = decodeDouble()

    override fun decodeStringElement(
        descriptor: SerialDescriptor,
        index: Int
    ): String = decodeString()

    override fun decodeInlineElement(
        descriptor: SerialDescriptor,
        index: Int
    ): Decoder = decodeInline(descriptor.getElementDescriptor(index))

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        try {
            for (annotation in descriptor.getElementAnnotations(index)) {
                if (annotation is Bits128) {
                    exactSize = 16
                    break
                } else if (annotation is Bits256) {
                    exactSize = 32
                    break
                } else if (annotation is TlFixedSize) {
                    exactSize = annotation.value
                    break
                }
            }
            return decodeSerializableValue(deserializer)
        } catch (e: Exception) {
            throw SerializationException(
                "Error decoding element at index $index in ${descriptor.serialName}: ${descriptor.getElementName(index)}",
                e
            )
        } finally {
            exactSize = -1 // Reset exactSize after reading
        }
    }

    @ExperimentalSerializationApi
    override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        for (annotation in descriptor.getElementAnnotations(index)) {
            if (annotation is TlConditional) {
                val flagFieldIndex = descriptor.getElementIndex(annotation.field)
                if (flagFieldIndex == CompositeDecoder.UNKNOWN_NAME) {
                    throw MissingFieldException(annotation.field, descriptor.serialName)
                }
                val flag = intValues[flagFieldIndex]
                return if (flag and annotation.mask != 0) {
                    decodeSerializableValue(deserializer)
                } else {
                    null
                }
            }
        }
        return null
    }

    @OptIn(InternalSerializationApi::class)
    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        if (deserializer.descriptor == ByteArraySerializer().descriptor) {
            if (exactSize > 0) {
                val byteArray = reader.readByteArray(exactSize)
                exactSize = -1 // Reset exactSize after reading
                @Suppress("UNCHECKED_CAST")
                return byteArray as T
            } else {
                @Suppress("UNCHECKED_CAST")
                return reader.readByteArray() as T
            }
        }
        if (deserializer !is AbstractPolymorphicSerializer<*>) {
            return deserializer.deserialize(this)
        }
        val constructorId = decodeInt()
        val constructor2name = deserializer.constructorIdToSerialName(tl)
        val typeName = constructor2name[constructorId]
            ?: error("Unknown constructor id 0x${constructorId.toHexString()} ($constructorId) for ${deserializer.descriptor.serialName}")

        @Suppress("UNCHECKED_CAST")
        val actualSerializer = deserializer.findPolymorphicSerializer(this, typeName)
                as DeserializationStrategy<T>
        return actualSerializer.deserialize(this)
    }
}
