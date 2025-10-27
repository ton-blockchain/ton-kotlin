package org.ton.sdk.blockchain.currency

import org.ton.kotlin.dict.Dictionary
import kotlin.jvm.JvmField

public class ExtraCurrencyCollection private constructor(
    private val map: Map<Int, ExtraCoins>,
    private var dict: Dictionary<Int, ExtraCoins>? = null
) : Map<Int, ExtraCoins> by map {
    public constructor(map: Map<Int, ExtraCoins>) : this(
        map, null
    )

    public constructor(dictionary: Dictionary<Int, ExtraCoins>) : this(
        dictionary, dictionary
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ExtraCurrencyCollection
        return map == other.map
    }

    override fun hashCode(): Int = map.hashCode()

    override fun toString(): String = "ExtraCurrencyCollection($map)"

    public companion object {
        @JvmField
        public val EMPTY: ExtraCurrencyCollection = ExtraCurrencyCollection(emptyMap())
    }
}
