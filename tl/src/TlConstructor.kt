package org.ton.kotlin.tl

import io.github.andreypfau.kotlinx.crypto.crc32.crc32
import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.*

public abstract class TlConstructor<T : Any>(
    schema: String,
    id: Int? = null,
) : TlCodec<T> {
    public val schema: String by lazy(LazyThreadSafetyMode.PUBLICATION) {
        schema
            .replace("(", "")
            .replace(")", "")
            .replace(";", "")
    }
    public val id: Int = id ?: crc32(this.schema.toByteArray())

    override fun encodeBoxed(writer: TlWriter, value: T) {
        writer.writeInt(id)
        encode(writer, value)
    }

    override fun decodeBoxed(reader: TlReader): T {
        val actualId = reader.readInt()
        require(actualId == id) {
            val idHex = id.reverseByteOrder().toUInt().toString(16).padStart(8, '0')
            val actualHex = actualId.reverseByteOrder().toUInt().toString(16).padStart(8, '0')
            "Invalid ID. expected: $idHex ($id) actual: $actualHex ($actualId)"
        }
        return decode(reader)
    }

    override fun toString(): String = schema

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TlConstructor<*>) return false
        if (id != other.id) return false
        if (schema != other.schema) return false
        return true
    }

    override fun hashCode(): Int = id
}
