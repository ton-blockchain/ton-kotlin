@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.dict

import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec
import kotlin.jvm.JvmStatic

public open class Dictionary<K, V>(
    public val dict: RawDictionary,
    protected val keyCodec: DictionaryKeyCodec<K>,
    protected val valueCodec: TlbCodec<V>,
) : Map<K, V> {
    init {
        require(dict.keySize == keyCodec.keySize)
    }

    public constructor(
        cell: Cell?,
        keyCodec: DictionaryKeyCodec<K>,
        valueCodec: TlbCodec<V>,
    ) : this(
        RawDictionary(cell, keyCodec.keySize),
        keyCodec,
        valueCodec,
    )

    public constructor(
        map: Map<out K, V>,
        keyCodec: DictionaryKeyCodec<K>,
        valueCodec: TlbCodec<V>,
        context: CellContext = CellContext.EMPTY
    ) : this(RawDictionary(keyCodec.keySize).apply {
        val builder = CellBuilder()
        map.forEach { (key, value) ->
            builder.reset()
            valueCodec.storeTlb(builder, value, context)
            val slice = builder.endCell().beginParse()
            val bitString = keyCodec.encodeKey(key)
            set(bitString, slice, context)
        }
    }, keyCodec, valueCodec)

    public constructor(
        dictionary: Dictionary<K, V>,
    ) : this(
        dictionary.dict.root,
        dictionary.keyCodec,
        dictionary.valueCodec,
    )

    public val cell: Cell? get() = dict.root

    override val size: Int
        get() = dict.iterator().asSequence().count()

    override val keys: Set<K>
        get() = loadKeys().toSet()

    override val values: Collection<V>
        get() = loadValues().toList()

    override val entries: Set<Map.Entry<K, V>>
        get() = loadEntries().toSet()

    public fun loadEntries(context: CellContext = CellContext.EMPTY): Sequence<Map.Entry<K, V>> =
        dict.iterator(context).asSequence().map { (k, vb) ->
            val value = valueCodec.loadTlb(vb, context)
            val key = keyCodec.decodeKey(k)
            DictEntry(key, value)
        }

    public fun loadKeys(context: CellContext = CellContext.EMPTY): Sequence<K> =
        dict.iterator(context).asSequence().map { (key, _) ->
            keyCodec.decodeKey(key)
        }

    public fun loadValues(context: CellContext = CellContext.EMPTY): Sequence<V> =
        dict.iterator(context).asSequence().map { (_, value) ->
            valueCodec.loadTlb(value, context)
        }

    override fun isEmpty(): Boolean {
        return dict.isEmpty()
    }

    override fun containsKey(key: K): Boolean {
        return dict[keyCodec.encodeKey(key)] != null
    }

    override fun containsValue(value: V): Boolean {
        return loadValues().contains(value)
    }

    override fun get(key: K): V? = get(key, CellContext.EMPTY)

    public fun get(key: K, context: CellContext = CellContext.EMPTY): V? {
        return valueCodec.loadTlb(dict.get(keyCodec.encodeKey(key), context) ?: return null, context)
    }

    public fun toMap(context: CellContext = CellContext.EMPTY): Map<K, V> {
        if (dict.isEmpty()) return emptyMap()
        return toMap(LinkedHashMap<K, V>(), context)
    }

    public fun <M : MutableMap<in K, in V>> toMap(destination: M, context: CellContext = CellContext.EMPTY): M {
        if (dict.isEmpty()) return destination
        dict.iterator(context).forEach { (key, value) ->
            val value = valueCodec.loadTlb(value, context)
            val key = keyCodec.decodeKey(key)
            destination[key] = value
        }
        return destination
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Dictionary<*, *>
        return dict == other.dict
    }

    override fun hashCode(): Int = dict.hashCode()

    override fun toString(): String = dict.toString()

    private inner class DictEntry(
        override val key: K,
        override val value: V,
    ) : Map.Entry<K, V>

    public companion object {
        @JvmStatic
        public fun <K, V> tlbCodec(
            keyCodec: DictionaryKeyCodec<K>,
            valueCodec: TlbCodec<V>
        ): TlbCodec<Dictionary<K, V>> {
            return DictionaryCodec(keyCodec, valueCodec)
        }
    }
}

private class DictionaryCodec<K, V>(
    private val keyCodec: DictionaryKeyCodec<K>,
    private val valueCodec: TlbCodec<V>,
) : TlbCodec<Dictionary<K, V>> {
    override fun loadTlb(slice: CellSlice, context: CellContext): Dictionary<K, V> {
        val root = slice.loadNullableRef()
        return Dictionary(root, keyCodec, valueCodec)
    }

    override fun storeTlb(builder: CellBuilder, value: Dictionary<K, V>, context: CellContext) {
        builder.storeNullableRef(value.cell)
    }
}

public fun <K, V> Map<K, V>.toDictionary(
    keyCodec: DictionaryKeyCodec<K>,
    valueCodec: TlbCodec<V>,
    context: CellContext = CellContext.EMPTY
): Dictionary<K, V> = Dictionary(this, keyCodec, valueCodec, context)
