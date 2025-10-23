package org.ton.kotlin.blockchain.message.address

/**
 * Account split depth. Fixed-length 5-bit integer of range `1..=30`
 */
public class SplitDepth(
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
        public val MIN: SplitDepth = SplitDepth(1)
        public val MAX: SplitDepth = SplitDepth(30)
    }
}
