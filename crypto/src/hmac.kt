package org.ton.sdk.crypto

public fun hmacSha256(key: ByteArray, message: ByteArray): ByteArray {
    return HMac(Sha256(), key).use { hmac ->
        hmac.update(message)
        hmac.digest()
    }
}

public fun hmacSha512(key: ByteArray, message: ByteArray): ByteArray {
    return HMac(Sha512(), key).use { hmac ->
        hmac.update(message)
        hmac.digest()
    }
}

private const val IPAD = 0x36
private const val OPAD = 0x5C

public class HMac(
    private val digest: Digest,
    key: ByteArray
) : AutoCloseable {
    public val macSize: Int
        get() = digest.digestSize
    public val blockSize: Int
        get() = digest.blockSize

    private val inputPad = ByteArray(blockSize)
    private val outputBuf = ByteArray(blockSize + macSize)

    init {
        digest.reset()
        var keyLength = key.size
        if (keyLength > blockSize) {
            digest.update(key)
            digest.digest(inputPad)
            keyLength = macSize
        } else {
            key.copyInto(inputPad)
        }

        inputPad.fill(0, keyLength)
        inputPad.copyInto(outputBuf)

        inputPad.indices.forEach {
            inputPad[it] = (inputPad[it].toInt() xor IPAD).toByte()
        }
        outputBuf.indices.forEach {
            outputBuf[it] = (outputBuf[it].toInt() xor OPAD).toByte()
        }
        digest.update(inputPad)
    }

    public fun digest(): ByteArray {
        val result = ByteArray(macSize)
        digest(result)
        return result
    }

    public fun digest(destination: ByteArray, destinationOffset: Int = 0) {
        val blockSize = blockSize
        digest.digest(outputBuf, blockSize)
        digest.update(outputBuf)
        digest.digest(destination, destinationOffset)
        outputBuf.fill(0, blockSize)
        digest.update(inputPad)
    }

    public fun update(source: ByteArray, startIndex: Int = 0, endIndex: Int = source.size): HMac = apply {
        digest.update(source, startIndex, endIndex)
    }

    public fun reset() {
        digest.reset()
        digest.update(inputPad)
    }

    override fun close() {
        digest.close()
    }
}
