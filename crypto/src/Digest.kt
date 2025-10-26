package org.ton.sdk.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import kotlinx.io.bytestring.unsafe.UnsafeByteStringOperations

public interface Digest : AutoCloseable {
    public val digestSize: Int
    public val blockSize: Int

    public fun update(source: ByteArray, startIndex: Int = 0, endIndex: Int = source.size): Digest

    @OptIn(UnsafeByteStringApi::class)
    public fun update(source: ByteString, startIndex: Int = 0, endIndex: Int = source.size): Digest {
        UnsafeByteStringOperations.withByteArrayUnsafe(source) {
            update(it, startIndex, endIndex)
        }
        return this
    }

    public fun digest(): ByteArray {
        val destination = ByteArray(digestSize)
        digest(destination)
        return destination
    }

    public fun digest(destination: ByteArray = ByteArray(digestSize), destinationOffset: Int = 0)

    public fun reset()

    override fun close()
}
