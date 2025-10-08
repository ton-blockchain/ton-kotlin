@file:Suppress("DEPRECATION")

package org.ton.tl.constructors

import org.ton.tl.*
import kotlin.reflect.KClass

@Deprecated(DEPRECATION_MESSAGE)
public open class EnumTlCombinator<T : Enum<T>>(
    override val baseClass: KClass<T>,
    values: List<Pair<T, String>>
) : AbstractTlCombinator<T>() {
    public constructor(baseClass: KClass<T>, vararg values: Pair<T, String>) : this(baseClass, values.toList())

    private val enum2Constructor: Map<T, TlCodec<out T>>
    private val constructor2Enum: Map<Int, TlCodec<out T>>

    init {
        enum2Constructor = values.asSequence().map {
            it.first to EnumConstructor(it.first, it.second)
        }.toMap()
        constructor2Enum = enum2Constructor.values.associateBy { it.id }
    }

    private class EnumConstructor<T : Enum<T>>(
        val enum: T,
        schema: String
    ) : TlConstructor<T>(schema) {
        override fun decode(reader: TlReader): T = enum

        override fun encode(writer: TlWriter, value: T) = Unit
    }

    override fun findConstructorOrNull(id: Int): TlDecoder<out T>? = constructor2Enum[id]

    override fun findConstructorOrNull(value: T): TlEncoder<T>? = enum2Constructor[value]?.cast()
}
