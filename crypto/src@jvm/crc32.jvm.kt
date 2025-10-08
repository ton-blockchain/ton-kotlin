package org.ton.kotlin.crypto

public actual class CRC32 actual constructor() : Digest {
    private val crc32jvm = java.util.zip.CRC32()

    public actual override val digestSize: Int
        get() = 4
    public actual override val blockSize: Int
        get() = 1

    public actual override fun update(source: ByteArray, startIndex: Int, endIndex: Int): CRC32 = apply {
        crc32jvm.update(source, startIndex, endIndex - startIndex)
    }

    public actual override fun digest(): ByteArray {
        val result = ByteArray(4)
        digest(result, 0)
        return result
    }

    public actual override fun digest(destination: ByteArray, destinationOffset: Int) {
        val intDigest = intDigest()
        destination[destinationOffset] = (intDigest shr 24 and 0xFF).toByte()
        destination[destinationOffset + 1] = (intDigest shr 16 and 0xFF).toByte()
        destination[destinationOffset + 2] = (intDigest shr 8 and 0xFF).toByte()
        destination[destinationOffset + 3] = (intDigest and 0xFF).toByte()
    }

    public actual override fun reset() {
        crc32jvm.reset()
    }

    public actual fun intDigest(): Int = crc32jvm.value.toInt()


    public actual override fun close() {
        reset()
    }
}
