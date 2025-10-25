package org.ton.sdk.bitstring

public class BitArray(
    private val data: ByteArray,
    private val dataOffset: Int,
    public val size: Int
) {
    public val byteSize: Int get() = (size + 7) ushr 3

    public operator fun get(index: Int): Boolean {
        if (index !in 0 until size) throw IndexOutOfBoundsException(
            "index ($index) is out of bit array bounds: [0..$size)"
        )
        val bitIndex = dataOffset + index
        return (data[bitIndex ushr 3].toInt() and (0x80 ushr (index and 7))) != 0
    }
}
