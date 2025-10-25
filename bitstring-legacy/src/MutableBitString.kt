package org.ton.bitstring

import kotlin.jvm.JvmStatic

public interface MutableBitString : BitString {
    public operator fun set(index: Int, bit: Int)
    public operator fun set(index: Int, element: Boolean): Boolean

    override operator fun plus(bits: BooleanArray): MutableBitString
    override operator fun plus(bits: Iterable<Boolean>): MutableBitString
    override operator fun plus(bits: Collection<Boolean>): MutableBitString
    override operator fun plus(bytes: ByteArray): MutableBitString
    override operator fun plus(bit: Boolean): MutableBitString
    override fun plus(bytes: ByteArray, bits: Int): MutableBitString

    public companion object {
        @JvmStatic
        public fun of(size: Int): MutableBitString = ByteBackedMutableBitString.of(size)
    }
}
