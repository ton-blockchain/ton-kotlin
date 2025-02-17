@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.dict

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.buildCell
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec
import kotlin.jvm.JvmStatic

public class AugmentedDictionary<K, A, V>(
    public val dict: Dictionary<K, Pair<A, V>>,
    public val extra: A,
) : Map<K, Pair<A, V>> by dict {
    override fun toString(): String = dict.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AugmentedDictionary<*, *, *>

        if (dict != other.dict) return false
        if (extra != other.extra) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dict.hashCode()
        result = 31 * result + (extra?.hashCode() ?: 0)
        return result
    }

    public companion object {
        @JvmStatic
        public fun <K, A, V> tlbCodec(
            dictCodec: TlbCodec<Dictionary<K, Pair<A, V>>>,
            extraCodec: TlbCodec<A>
        ): TlbCodec<AugmentedDictionary<K, A, V>> {
            return AugmentedDictionaryCodec(dictCodec, extraCodec)
        }

        @JvmStatic
        public fun <K, A, V> tlbCodec(
            keyCodec: DictionaryKeyCodec<K>,
            extraCodec: TlbCodec<A>,
            pairCodec: TlbCodec<Pair<A, V>>
        ): TlbCodec<AugmentedDictionary<K, A, V>> {
            val dictCodec = Dictionary.tlbCodec(keyCodec, pairCodec)
            return tlbCodec(dictCodec, extraCodec)
        }

        @JvmStatic
        public fun <K, A, V> loadFromRoot(
            slice: CellSlice,
            keyCodec: DictionaryKeyCodec<K>,
            extraCodec: TlbCodec<A>,
            valueCodec: TlbCodec<V>,
            context: CellContext = CellContext.EMPTY
        ): AugmentedDictionary<K, A, V> {
            val root = slice.copy()

            val keySize = keyCodec.keySize
            val label = slice.readLabel(keySize)
            val extra = if (label.size != keySize) {
                slice.skipRefs(2)
                extraCodec.loadTlb(slice)
            } else {
                val extra = extraCodec.loadTlb(slice)
                valueCodec.loadTlb(slice)
                extra
            }

            val bitsCount = root.remainingBits - slice.remainingBits
            val refsCount = root.remainingRefs - slice.remainingRefs

            val cell = buildCell(context) {
                storeBitString(root.preloadBitString(bitsCount))
                repeat(refsCount) { index ->
                    storeRef(root.preloadRef(index))
                }
            }
            val pairCodec = TlbCodec.pair(extraCodec, valueCodec)
            return AugmentedDictionary(Dictionary(cell, keyCodec, pairCodec), extra)
        }
    }
}

private class AugmentedDictionaryCodec<K, A, V>(
    private val dictCodec: TlbCodec<Dictionary<K, Pair<A, V>>>,
    private val extraCodec: TlbCodec<A>,
) : TlbCodec<AugmentedDictionary<K, A, V>> {
    override fun loadTlb(slice: CellSlice, context: CellContext): AugmentedDictionary<K, A, V> {
        val dict = dictCodec.loadTlb(slice, context)
        val extra = extraCodec.loadTlb(slice, context)
        return AugmentedDictionary(dict, extra)
    }

    override fun storeTlb(builder: CellBuilder, value: AugmentedDictionary<K, A, V>, context: CellContext) {
        dictCodec.storeTlb(builder, value.dict, context)
        extraCodec.storeTlb(builder, value.extra, context)
    }
}