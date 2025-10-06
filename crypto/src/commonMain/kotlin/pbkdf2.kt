package org.ton.kotlin.crypto

import kotlin.experimental.xor

public fun pbkdf2sha256(
    password: ByteArray,
    salt: ByteArray,
    iterationCount: Int,
    destination: ByteArray
) {
    Pbkdf2(Sha256(), password, salt, iterationCount).use { pbkdf2 ->
        pbkdf2.deriveKey(destination, keySize = destination.size)
    }
}

public fun pbkdf2sha512(
    password: ByteArray,
    salt: ByteArray,
    iterationCount: Int,
    destination: ByteArray
) {
    Pbkdf2(Sha512(), password, salt, iterationCount).use { pbkdf2 ->
        pbkdf2.deriveKey(destination, keySize = destination.size)
    }
}

public class Pbkdf2(
    private val hMac: HMac,
    private val salt: ByteArray,
    private val iterationCount: Int
) : AutoCloseable {
    public constructor(
        digest: Digest,
        password: ByteArray,
        salt: ByteArray,
        iterationCount: Int
    ) : this(HMac(digest, password), salt, iterationCount)

    private val state = ByteArray(hMac.macSize)

    public fun deriveKey(keySize: Int = hMac.macSize): ByteArray {
        val key = ByteArray(keySize)
        deriveKey(key, keySize = keySize)
        return key
    }

    public fun deriveKey(destination: ByteArray, destinationOffset: Int = 0, keySize: Int = hMac.macSize) {
        require(iterationCount >= 1) { "iterationCount must be >= 1" }
        require(keySize >= 0) { "keySize must be >= 0" }
        require(destinationOffset >= 0) { "destinationOffset must be >= 0" }
        require(destinationOffset + keySize <= destination.size) {
            "destination too small for the requested key"
        }

        hMac.reset()

        val digestSize = hMac.macSize
        val numBlocks = (keySize + digestSize - 1) / digestSize

        val buf = ByteArray(4) // INT(i) big-endian
        var outPos = destinationOffset
        val endPos = destinationOffset + keySize

        for (block in 1..numBlocks) {
            val blockLen = minOf(digestSize, endPos - outPos)

            // U1 = PRF(P, S || INT(block))
            buf[0] = (block ushr 24).toByte()
            buf[1] = (block ushr 16).toByte()
            buf[2] = (block ushr 8).toByte()
            buf[3] = block.toByte()

            hMac.update(salt)
            hMac.update(buf)
            hMac.digest(state) // state = U1

            // T = U1
            state.copyInto(destination, outPos, 0, blockLen)

            // Ui = PRF(P, Ui-1); T ^= Ui
            var i = 1
            while (i < iterationCount) {
                hMac.update(state)
                hMac.digest(state)
                var j = 0
                while (j < blockLen) {
                    destination[outPos + j] = (destination[outPos + j] xor state[j])
                    j++
                }
                i++
            }

            outPos += blockLen
        }
    }

    override fun close() {
        hMac.close()
        state.fill(0)
    }
}
