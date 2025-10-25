package org.ton.sdk.cell

import kotlin.jvm.JvmStatic

/**
 * _de Brujn_ level presence bitset.
 */
public class LevelMask(
    mask: Int = 0
) {
    public val mask: Int = mask and 0b111

    /**
     * Counts presented higher hashes.
     */
    public val level: Int get() = 32 - mask.countLeadingZeroBits()

    public val hashIndex: Int get() = mask.countOneBits()

    public val hashCount: Int get() = hashIndex + 1

    /**
     * Applies the level mask to the given level, returning a new [LevelMask] instance.
     */
    public fun apply(level: Int): LevelMask {
        require(level < 32)
        return LevelMask(mask and ((1 shl level) - 1))
    }

    /**
     * Determines if the given level is significant within the current level mask.
     */
    public fun isSignificant(level: Int): Boolean {
        require(level < 32)
        val result = level == 0 || ((mask shr (level - 1)) % 2 != 0)
        check(result == (apply(level).level == level))
        return result
    }

    public infix fun or(other: LevelMask): LevelMask =
        LevelMask(mask or other.mask)

    public infix fun shr(bitCount: Int): LevelMask =
        LevelMask(mask shr bitCount)

    /**
     * Returns true if there are no levels in mask.
     */
    public fun isEmpty(): Boolean = mask == 0

    /**
     * Creates a new mask, shifted by the offset.
     */
    public fun virtualize(offset: Int = 1): LevelMask =
        LevelMask(mask ushr offset)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LevelMask) return false
        return mask == other.mask
    }

    override fun hashCode(): Int = mask

    override fun toString(): String = mask.toString(2)

    public companion object {
        private val ZERO = LevelMask(0)

        /**
         * Max _de Brujn_ level.
         */
        public const val MAX_LEVEL: Int = 3

        @JvmStatic
        public fun level(level: Int): LevelMask {
            require(level < 32)
            if (level == 0) return ZERO
            return LevelMask((1 shl (level - 1)))
        }
    }
}
