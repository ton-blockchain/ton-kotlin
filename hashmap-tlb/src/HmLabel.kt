@file:Suppress("OPT_IN_USAGE", "NOTHING_TO_INLINE")

package org.ton.kotlin.hashmap

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.tlb.TlbNegatedCodec
import org.ton.kotlin.tlb.TlbNegatedCombinator
import org.ton.kotlin.tlb.TlbNegatedConstructor
import org.ton.kotlin.tlb.TlbObject
import kotlin.jvm.JvmStatic

public inline fun HmLabel(key: BitString, max: Int = key.size): HmLabel = HmLabel.of(key, max)

@Serializable
@JsonClassDiscriminator("@type")
public sealed interface HmLabel : TlbObject {
    public fun toBitString(): BitString

    public companion object {
        @JvmStatic
        public fun empty(): HmLabel = HmlShort.empty()

        @JvmStatic
        public fun of(key: BitString, max: Int = key.size): HmLabel {
            if (key.isEmpty()) return empty()
            val len = 16 - max.toShort().countLeadingZeroBits()
            val longLength = 2 + len + key.size
            val shortLength = 1 + 2 * key.size + 1
            val sameLength = 2 + 1 + len
            val sameLabel = HmlSame.of(key)
            if (sameLabel != null) {
                val longOrShortLength = minOf(longLength, shortLength)
                if (sameLength < longOrShortLength) {
                    return sameLabel
                }
            }
            if (shortLength <= longLength) {
                return HmlShort(key)
            }
            return HmlLong(key)
        }

        @JvmStatic
        public fun tlbCodec(m: Int): TlbNegatedCodec<HmLabel> = if (m < 16) {
            HashMapLabelTlbCombinator.CACHE[m]
        } else {
            HashMapLabelTlbCombinator(m)
        }
    }
}

private class HashMapLabelTlbCombinator(
    m: Int,
    hmlLong: TlbNegatedConstructor<HmlLong> = HmlLong.tlbCodec(m),
    hmlShort: TlbNegatedConstructor<HmlShort> = HmlShort.tlbCodec(),
    hmlSame: TlbNegatedConstructor<HmlSame> = HmlSame.tlbCodec(m)
) : TlbNegatedCombinator<HmLabel>(
    HmLabel::class,
    HmlLong::class to hmlLong,
    HmlShort::class to hmlShort,
    HmlSame::class to hmlSame,
) {
    companion object {
        val CACHE = Array(16) { HashMapLabelTlbCombinator(it) }
    }
}
