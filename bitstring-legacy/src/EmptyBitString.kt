package org.ton.bitstring

internal object EmptyBitString : BitString {
    override val size: Int get() = 0

    override fun get(index: Int): Boolean = throw IndexOutOfBoundsException()

    override fun getOrNull(index: Int): Boolean? = null

    override fun countLeadingBits(fromIndex: Int, toIndex: Int, bit: Boolean): Int = 0

    override fun plus(bits: BooleanArray): BitString = BitString(*bits)

    override fun plus(bits: Iterable<Boolean>): BitString = BitString(bits)

    override fun plus(bits: BitString): BitString = bits

    override fun plus(bits: Collection<Boolean>): BitString = BitString(bits)

    override fun plus(bytes: ByteArray): BitString = BitString(bytes)

    override fun plus(bytes: ByteArray, bits: Int): BitString = BitString(bytes, bits)

    override fun slice(indices: IntRange): BitString {
        if (indices.first == 0 && indices.last == 0) return this
        throw IndexOutOfBoundsException(indices.toString())
    }

    override fun substring(startIndex: Int, endIndex: Int): BitString {
        if (startIndex == 0 && endIndex == 0) return this
        throw IndexOutOfBoundsException((startIndex..endIndex).toString())
    }

    override fun toByteArray(augment: Boolean): ByteArray =
        byteArrayOf()

    override fun toBooleanArray(): BooleanArray = booleanArrayOf()

    override fun toMutableBitString(): MutableBitString = ByteBackedMutableBitString(ByteArray(1), 0)

    override fun xor(other: BitString): BitString = other

    override fun or(other: BitString): BitString = other

    override fun toString(): String = "x{}"

    override fun toHexString(): String = ""

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is BitString) return false
        if (other.size != 0) return false
        return true
    }

    override fun iterator(): Iterator<Boolean> = iterator { }

    override fun compareTo(other: BitString): Int = -1
}
