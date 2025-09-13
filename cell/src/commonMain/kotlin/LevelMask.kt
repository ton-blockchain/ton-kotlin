package org.ton.kotlin.cell

import kotlin.jvm.JvmStatic

public class LevelMask(
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

    public fun d1(referenceCount: Int): Byte =
        ((mask shl 5) or (referenceCount and 0b111)).toByte()

    override fun toString(): String = mask.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LevelMask) return false
        return mask == other.mask
    }

    override fun hashCode(): Int = mask

    public companion object {
        public val ZERO: LevelMask = LevelMask(0)

        @JvmStatic
        public fun level(level: Int): LevelMask {
            require(level < 32)
            if (level == 0) return ZERO
            return LevelMask(1 shl (level - 1))
        }
    }
}
