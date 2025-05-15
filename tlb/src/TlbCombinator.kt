package org.ton.kotlin.tlb

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.exception.UnknownTlbConstructorException
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import kotlin.reflect.KClass

public abstract class TlbCombinator<T : Any>(
    override val baseClass: KClass<T>,
    vararg subClasses: Pair<KClass<out T>, TlbCodec<out T>>
) : AbstractTlbCombinator<T>(), TlbCombinatorProvider<T> {
    private val class2codec: MutableMap<KClass<out T>, TlbCodec<out T>>
    private val trie = TlbTrie<TlbCodec<out T>>()

    init {
        class2codec = subClasses.toMap().toMutableMap()

        subClasses.forEach { (_, constructor) ->
            when (constructor) {
                is TlbConstructor<out T> -> addConstructor(constructor)
                is TlbConstructorProvider<out T> -> addConstructor(constructor.tlbConstructor())
                is TlbCombinator<out T> -> addCombinator(constructor)
                is TlbCombinatorProvider<out T> -> addCombinator(constructor.tlbCombinator())
            }
        }
    }

    private fun addConstructor(constructor: TlbConstructor<out T>) {
        trie[constructor.id] = constructor
    }

    private fun addCombinator(combinator: TlbCombinator<out T>) {
        combinator.class2codec.forEach { (_, constructor) ->
            when (constructor) {
                is TlbConstructor<out T> -> addConstructor(constructor)
                is TlbConstructorProvider<out T> -> addConstructor(constructor.tlbConstructor())
            }
        }
        class2codec.putAll(combinator.class2codec)
    }

    override fun tlbCombinator(): TlbCombinator<T> = this

    override fun loadTlb(slice: CellSlice, context: CellContext): T {
        val constructor = findTlbLoaderOrNull(slice) ?: throw UnknownTlbConstructorException(
            slice.preloadBitString(32)
        )
        if (constructor is TlbConstructor<*>) {
            slice.skipBits(constructor.id.size)
        }
        return constructor.loadTlb(slice, context)
    }

    override fun storeTlb(builder: CellBuilder, value: T, context: CellContext) {
        val storer = findTlbStorerOrNull(value) ?: throw UnknownTlbConstructorException()
        if (storer is TlbConstructorProvider<*>) {
            builder.storeBitString(storer.tlbConstructor().id)
        } else if (storer is TlbConstructor<*>) {
            builder.storeBitString(storer.id)
        }
        return storer.storeTlb(builder, value, context)
    }

    protected open fun findTlbLoaderOrNull(cellSlice: CellSlice): TlbLoader<out T>? {
        return trie[cellSlice.bits, cellSlice.bitsPosition]
    }

    protected open fun findTlbLoaderOrNull(bitString: BitString): TlbLoader<out T>? {
        return trie[bitString]
    }

    @Suppress("UNCHECKED_CAST")
    protected open fun findTlbStorerOrNull(value: T): TlbStorer<T>? {
        val constructor = class2codec[value::class]
            ?: return null
        return constructor as TlbStorer<T>
    }

    private data class TlbTrie<T>(
        var left: TlbTrie<T>? = null,
        var right: TlbTrie<T>? = null,
        var value: T? = null
    ) {
        operator fun set(key: BitString, value: T) {
            var x = this
            for (i in 0 until key.size) {
                x = if (key[i]) {
                    x.right ?: TlbTrie<T>().also {
                        x.right = it
                    }
                } else {
                    x.left ?: TlbTrie<T>().also {
                        x.left = it
                    }
                }
            }
            x.value = value
        }

        operator fun get(key: BitString, offset: Int = 0): T? {
            var x = this
            for (i in offset until key.size) {
                if (key[i]) {
                    x.right?.also {
                        x = it
                    } ?: break
                } else {
                    x.left?.also {
                        x = it
                    } ?: break
                }
            }
            return x.value
        }
    }
}
