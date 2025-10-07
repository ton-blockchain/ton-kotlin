package org.ton.tl

import kotlin.reflect.KClass

@Deprecated(DEPRECATION_MESSAGE)
public abstract class TlCombinator<T : Any> private constructor(
    override val baseClass: KClass<T>,
    constructors: List<Pair<KClass<out T>, TlConstructor<out T>>>
) : AbstractTlCombinator<T>() {
    public constructor(baseClass: KClass<T>, vararg constructors: Pair<KClass<out T>, TlConstructor<out T>>) : this(
        baseClass,
        constructors.toList()
    )

    private val class2Constructor: Map<KClass<out T>, TlConstructor<out T>> = constructors.toMap()
    private val id2Constructor: Map<Int, TlCodec<out T>> by lazy {
        class2Constructor.entries
            .groupingBy { it.value.id }
            .aggregate<Map.Entry<KClass<out T>, TlConstructor<out T>>, Int, Map.Entry<KClass<*>, TlConstructor<out T>>> { _, accumulator, element, _ ->
                if (accumulator != null) {
                    throw IllegalArgumentException("Duplicate ID: ${element.key}")
                }
                element
            }
            .mapValues { it.value.value }
    }

    override fun findConstructorOrNull(id: Int): TlDecoder<out T>? = id2Constructor[id]

    override fun findConstructorOrNull(value: T): TlEncoder<T>? = class2Constructor[value::class]?.cast()
}
