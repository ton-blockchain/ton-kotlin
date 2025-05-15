package org.ton.kotlin.cell

import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

@JvmInline
public value class LevelMask(
    public val mask: Int = 0
) {
    public val level: Int get() = Int.SIZE_BITS - mask.countLeadingZeroBits()
    public val hashIndex: Int get() = mask.countOneBits()
    public val hashCount: Int get() = hashIndex + 1

    public fun apply(level: Int): LevelMask {
        require(level < 32)
        return LevelMask(mask and ((1 shl level) - 1))
    }

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

    public fun isEmpty(): Boolean = mask == 0

    public fun virtualize(offset: Int = 1): LevelMask =
        LevelMask(mask ushr offset)

    public companion object {
        private val ZERO = LevelMask(0)

        @JvmStatic
        public fun level(level: Int): LevelMask {
            require(level < 32)
            if (level == 0) return ZERO
            return LevelMask(1 shl (level - 1))
        }
    }
}
