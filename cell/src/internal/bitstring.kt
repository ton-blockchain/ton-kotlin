package org.ton.sdk.cell.internal

internal fun storeLong(
    dest: ByteArray,
    destOffset: Int,
    bitOffset: Int,
    value: Long,
    bits: Int,
) {
    require(bitOffset in 0..7)
    require(bits in 0..64)
    if (bits == 0) return

    // Normalize the value to the lowest `bits` bits so negative numbers work as expected for partial widths
    val normalized = if (bits == 64) value else value and ((1L shl bits) - 1)

    var totalBit = destOffset * 8 + bitOffset
    var i = 0
    while (i < bits) {
        val srcBit = bits - 1 - i // take MSB-first from normalized
        val bit = ((normalized ushr srcBit) and 1L).toInt()

        val byteIndex = totalBit ushr 3
        val bitInByte = 7 - (totalBit and 7)
        val mask = 1 shl bitInByte
        if (bit == 1) {
            dest[byteIndex] = (dest[byteIndex].toInt() or mask).toByte()
        } else {
            dest[byteIndex] = (dest[byteIndex].toInt() and mask.inv()).toByte()
        }

        totalBit++
        i++
    }
}

/** Writes a 64-bit integer in big-endian order. */
@Suppress("NOTHING_TO_INLINE")
private inline fun writeLongBE(dest: ByteArray, destOffset: Int, v: Long) {
    dest[destOffset + 0] = (v ushr 56).toByte()
    dest[destOffset + 1] = (v ushr 48).toByte()
    dest[destOffset + 2] = (v ushr 40).toByte()
    dest[destOffset + 3] = (v ushr 32).toByte()
    dest[destOffset + 4] = (v ushr 24).toByte()
    dest[destOffset + 5] = (v ushr 16).toByte()
    dest[destOffset + 6] = (v ushr 8).toByte()
    dest[destOffset + 7] = (v ushr 0).toByte()
}

/** Writes a 32-bit integer in big-endian order. */
@Suppress("NOTHING_TO_INLINE")
private inline fun writeIntBE(dest: ByteArray, destOffset: Int, v: Int) {
    dest[destOffset + 0] = (v ushr 24).toByte()
    dest[destOffset + 1] = (v ushr 16).toByte()
    dest[destOffset + 2] = (v ushr 8).toByte()
    dest[destOffset + 3] = (v ushr 0).toByte()
}
