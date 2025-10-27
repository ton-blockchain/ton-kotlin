package org.ton.sdk.blockchain.address

import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Account split depth. Fixed-length 5-bit integer of range `1..=30`
 */
public class SplitDepth(
    @get:JvmName("value")
    public val value: Int
) {
    init {
        require(value in 1..30)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SplitDepth
        return value == other.value
    }

    override fun hashCode(): Int = value

    override fun toString(): String = value.toString()

    public companion object {
        @JvmField
        public val MIN: SplitDepth = SplitDepth(1)

        @JvmField
        public val MAX: SplitDepth = SplitDepth(30)
    }
}
