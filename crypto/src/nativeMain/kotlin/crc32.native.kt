package org.ton.kotlin.crypto

public actual class CRC32 : Digest {
    private val engine = CRC32Engine(IEEE_TABLE_ARRAY)
    actual override val digestSize: Int
        get() = engine.digestSize
    actual override val blockSize: Int
        get() = engine.blockSize

    actual override fun update(
        source: ByteArray,
        startIndex: Int,
        endIndex: Int
    ): CRC32 = apply {
        engine.update(source, startIndex, endIndex)
    }

    actual override fun digest(destination: ByteArray, destinationOffset: Int) {
        engine.digest(destination, destinationOffset)
    }

    actual override fun reset() {
        engine.reset()
    }

    actual override fun close() {
        engine.close()
    }

    public actual fun intDigest(): Int {
        return engine.intDigest()
    }
}
