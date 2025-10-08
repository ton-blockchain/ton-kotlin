@file:Suppress("DEPRECATION")

package org.ton.tl

import kotlin.reflect.KClass

@Deprecated(DEPRECATION_MESSAGE)
public abstract class AbstractTlCombinator<T : Any> : TlCodec<T> {
    public abstract val baseClass: KClass<T>

    override fun decode(reader: TlReader): T = decodeBoxed(reader)

    override fun decodeBoxed(reader: TlReader): T {
        val id = reader.readInt()
        val constructor = findConstructorOrNull(id)
        requireNotNull(constructor) { "Unknown constructor ID: $id" }
        return constructor.decode(reader)
    }

    override fun encode(writer: TlWriter, value: T) {
        encodeBoxed(writer, value)
    }

    override fun encodeBoxed(writer: TlWriter, value: T) {
        val constructor = findConstructorOrNull(value)
        requireNotNull(constructor) { "Unknown constructor for type: ${value::class}" }
        constructor.encodeBoxed(writer, value)
    }

    public abstract fun findConstructorOrNull(id: Int): TlDecoder<out T>?

    public abstract fun findConstructorOrNull(value: T): TlEncoder<T>?
}
