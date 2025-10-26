package org.ton.sdk.crypto

import kotlin.experimental.xor

public fun crc32(bytes: ByteArray): Int {
    return CRC32().use {
        it.update(bytes)
        it.intDigest()
    }
}

public fun crc32c(bytes: ByteArray): Int {
    return CRC32C().use {
        it.update(bytes)
        it.intDigest()
    }
}

public expect class CRC32 public constructor() : Digest {
    public fun intDigest(): Int

    override val digestSize: Int

    override val blockSize: Int

    public override fun update(source: ByteArray, startIndex: Int, endIndex: Int): CRC32

    public override fun digest(): ByteArray

    public override fun digest(destination: ByteArray, destinationOffset: Int)

    public override fun reset()

    override fun close()
}

public class CRC32C : Digest {
    private val engine = CRC32Engine(CASTAGNOLI_TABLE)
    override val digestSize: Int
        get() = engine.digestSize
    override val blockSize: Int
        get() = engine.blockSize

    override fun update(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): CRC32C = apply {
        engine.update(source, startIndex, endIndex)
    }

    override fun digest(destination: ByteArray, destinationOffset: Int) {
        engine.digest(destination, destinationOffset)
    }

    override fun reset() {
        engine.reset()
    }

    override fun close() {
        engine.close()
    }

    public fun intDigest(): Int {
        return engine.intDigest()
    }
}

internal open class CRC32Engine internal constructor(
    private val table: IntArray
) : Digest {
    override val digestSize: Int
        get() = 4
    override val blockSize: Int
        get() = 1

    private var crc32: Int = 0xffffffff.toInt()

    fun intDigest(): Int = crc32.xor(0xffffffff.toInt())

    override fun update(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): CRC32Engine = apply {
        for (i in startIndex until endIndex) {
            val index = source[i].xor(crc32.toByte()).toUByte()
            crc32 = table[index.toInt()].xor(crc32.ushr(8))
        }
    }

    override fun digest(): ByteArray {
        val result = ByteArray(digestSize)
        digest(result)
        return result
    }

    override fun digest(destination: ByteArray, destinationOffset: Int) {
        val intDigest = intDigest()
        destination[destinationOffset] = (intDigest shr 24 and 0xFF).toByte()
        destination[destinationOffset + 1] = (intDigest shr 16 and 0xFF).toByte()
        destination[destinationOffset + 2] = (intDigest shr 8 and 0xFF).toByte()
        destination[destinationOffset + 3] = (intDigest and 0xFF).toByte()
    }

    override fun reset() {
        crc32 = 0xffffffff.toInt()
    }

    override fun close() {
        reset()
    }
}

// IEEE is by far and away the most common CRC-32 polynomial.
// Used by ethernet (IEEE 802.3), v.42, fddi, gzip, zip, png, ...
internal val IEEE_TABLE_ARRAY = generateCrc32Table(0xEDB88320.toInt())

// Castagnoli's polynomial, used in iSCSI.
// Has better error detection characteristics than IEEE.
// https://dx.doi.org/10.1109/26.231911
internal val CASTAGNOLI_TABLE = generateCrc32Table(0x82F63B78.toInt())

private fun generateCrc32Table(poly: Int): IntArray {
    val table = IntArray(256)

    for (idx in table.indices) {
        table[idx] = idx
        for (bit in 8 downTo 1) {
            table[idx] = if (table[idx] % 2 == 0) {
                table[idx].ushr(1)
            } else {
                table[idx].ushr(1).xor(poly)
            }
        }
    }

    return table
}
