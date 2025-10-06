package org.ton.kotlin.crypto

public interface Digest : AutoCloseable {
    public val digestSize: Int
    public val blockSize: Int

    public fun update(source: ByteArray, startIndex: Int = 0, endIndex: Int = source.size): Digest

    public fun digest(): ByteArray {
        val destination = ByteArray(digestSize)
        digest(destination)
        return destination
    }

    public fun digest(destination: ByteArray = ByteArray(digestSize), destinationOffset: Int = 0)

    public fun reset()

    override fun close()
}
