@file:Suppress("NOTHING_TO_INLINE")

package org.ton.sdk.crypto

import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import kotlinx.io.bytestring.unsafe.UnsafeByteStringOperations

public actual class Sha256 : GeneralDigest {
    public actual constructor() : super()

    private var h1 = 0
    private var h2 = 0
    private var h3 = 0
    private var h4 = 0
    private var h5 = 0
    private var h6 = 0
    private var h7 = 0
    private var h8 = 0
    private val x = IntArray(64)
    private var xOff = 0

    init {
        reset()
    }

    actual override val digestSize: Int get() = SIZE_BYTES

    actual override val blockSize: Int get() = BLOCK_SIZE_BYTES

    actual override fun update(source: ByteArray, startIndex: Int, endIndex: Int): Sha256 = apply {
        super.update(source, startIndex, endIndex)
    }

    override fun digest(): ByteArray {
        val result = ByteArray(SIZE_BYTES)
        digest(result)
        return result
    }

    actual override fun digest(destination: ByteArray, destinationOffset: Int) {
        finish()
        destination.setIntAt(destinationOffset, h1)
        destination.setIntAt(destinationOffset + 4, h2)
        destination.setIntAt(destinationOffset + 8, h3)
        destination.setIntAt(destinationOffset + 12, h4)
        destination.setIntAt(destinationOffset + 16, h5)
        destination.setIntAt(destinationOffset + 20, h6)
        destination.setIntAt(destinationOffset + 24, h7)
        destination.setIntAt(destinationOffset + 28, h8)
        reset()
    }

    actual override fun reset() {
        super.reset()

        /**
         * SHA-256 initial hash value
         * The first 32 bits of the fractional parts of the square roots
         * of the first eight prime numbers
         */
        h1 = 0x6a09e667
        h2 = 0xbb67ae85.toInt()
        h3 = 0x3c6ef372
        h4 = 0xa54ff53a.toInt()
        h5 = 0x510e527f
        h6 = 0x9b05688c.toInt()
        h7 = 0x1f83d9ab
        h8 = 0x5be0cd19

        xOff = 0
        x.fill(0)
    }


    override fun processWord(input: ByteArray, offset: Int) {
        var n = input[offset].toInt() shl 24
        n = n or ((input[offset + 1].toInt() and 0xFF) shl 16)
        n = n or ((input[offset + 2].toInt() and 0xFF) shl 8)
        n = n or (input[offset + 3].toInt() and 0xFF)
        x[xOff] = n
        if (++xOff == 16) {
            processBlock()
        }
    }

    override fun processLength(bitLength: Long) {
        if (xOff > 14) {
            processBlock()
        }
        x[14] = (bitLength ushr 32).toInt()
        x[15] = (bitLength and 0xffffffff).toInt()
    }

    override fun processBlock() {
        // expand 16 word block into 64 word blocks.
        for (t in 16 until 64) {
            x[t] = Theta1(x[t - 2]) + x[t - 7] + Theta0(
                x[t - 15]
            ) + x[t - 16]
        }

        // set up working variables.
        var a = h1
        var b = h2
        var c = h3
        var d = h4
        var e = h5
        var f = h6
        var g = h7
        var h = h8

        var t = 0
        for (i in 0 until 8) {
            // t = 8 * i
            h += Sum1(e) + Ch(
                e,
                f,
                g
            ) + k[t] + x[t]
            d += h
            h += Sum0(a) + Maj(a, b, c)
            ++t

            // t = 8 * i + 1
            g += Sum1(d) + Ch(
                d,
                e,
                f
            ) + k[t] + x[t]
            c += g
            g += Sum0(h) + Maj(h, a, b)
            ++t

            // t = 8 * i + 2
            f += Sum1(c) + Ch(
                c,
                d,
                e
            ) + k[t] + x[t]
            b += f
            f += Sum0(g) + Maj(g, h, a)
            ++t

            // t = 8 * i + 3
            e += Sum1(b) + Ch(
                b,
                c,
                d
            ) + k[t] + x[t]
            a += e
            e += Sum0(f) + Maj(f, g, h)
            ++t

            // t = 8 * i + 4
            d += Sum1(a) + Ch(
                a,
                b,
                c
            ) + k[t] + x[t]
            h += d
            d += Sum0(e) + Maj(e, f, g)
            ++t

            // t = 8 * i + 5
            c += Sum1(h) + Ch(
                h,
                a,
                b
            ) + k[t] + x[t]
            g += c
            c += Sum0(d) + Maj(d, e, f)
            ++t

            // t = 8 * i + 6
            b += Sum1(g) + Ch(
                g,
                h,
                a
            ) + k[t] + x[t]
            f += b
            b += Sum0(c) + Maj(c, d, e)
            ++t

            // t = 8 * i + 7
            a += Sum1(f) + Ch(
                f,
                g,
                h
            ) + k[t] + x[t]
            e += a
            a += Sum0(b) + Maj(b, c, d)
            ++t
        }

        h1 += a
        h2 += b
        h3 += c
        h4 += d
        h5 += e
        h6 += f
        h7 += g
        h8 += h

        // reset the offset and clean out the word buffer.
        xOff = 0
        x.fill(0)
    }

    actual override fun close() {
        reset()
    }

    public companion object {
        public const val ALGORITHM_NAME: String = "SHA-256"
        public const val SIZE_BYTES: Int = 32
        public const val SIZE_BITS: Int = SIZE_BYTES * Byte.SIZE_BITS
        public const val BLOCK_SIZE_BYTES: Int = 64
        public const val BLOCK_SIZE_BITS: Int = BLOCK_SIZE_BYTES * Byte.SIZE_BITS

        private fun ByteArray.setIntAt(offset: Int, value: Int) {
            this[offset] = (value ushr 24).toByte()
            this[offset + 1] = (value ushr 16).toByte()
            this[offset + 2] = (value ushr 8).toByte()
            this[offset + 3] = value.toByte()
        }
    }

    @OptIn(UnsafeByteStringApi::class)
    public actual fun digestToHashBytes(): HashBytes =
        HashBytes(UnsafeByteStringOperations.wrapUnsafe(digest()))

}

/**
 * SHA-256 Constants
 * (represent the first 32 bits of the fractional parts of the
 * cube roots of the first sixty-four prime numbers)
 */
private val k = uintArrayOf(
    // @formatter:off
    0x428a2f98u, 0x71374491u, 0xb5c0fbcfu, 0xe9b5dba5u, 0x3956c25bu, 0x59f111f1u, 0x923f82a4u, 0xab1c5ed5u,
    0xd807aa98u, 0x12835b01u, 0x243185beu, 0x550c7dc3u, 0x72be5d74u, 0x80deb1feu, 0x9bdc06a7u, 0xc19bf174u,
    0xe49b69c1u, 0xefbe4786u, 0x0fc19dc6u, 0x240ca1ccu, 0x2de92c6fu, 0x4a7484aau, 0x5cb0a9dcu, 0x76f988dau,
    0x983e5152u, 0xa831c66du, 0xb00327c8u, 0xbf597fc7u, 0xc6e00bf3u, 0xd5a79147u, 0x06ca6351u, 0x14292967u,
    0x27b70a85u, 0x2e1b2138u, 0x4d2c6dfcu, 0x53380d13u, 0x650a7354u, 0x766a0abbu, 0x81c2c92eu, 0x92722c85u,
    0xa2bfe8a1u, 0xa81a664bu, 0xc24b8b70u, 0xc76c51a3u, 0xd192e819u, 0xd6990624u, 0xf40e3585u, 0x106aa070u,
    0x19a4c116u, 0x1e376c08u, 0x2748774cu, 0x34b0bcb5u, 0x391c0cb3u, 0x4ed8aa4au, 0x5b9cca4fu, 0x682e6ff3u,
    0x748f82eeu, 0x78a5636fu, 0x84c87814u, 0x8cc70208u, 0x90befffau, 0xa4506cebu, 0xbef9a3f7u, 0xc67178f2u,
    // @formatter:on
).asIntArray()

private inline fun Ch(x: Int, y: Int, z: Int): Int = (x and y) xor (x.inv() and z)

private inline fun Maj(x: Int, y: Int, z: Int): Int = (x and y) xor (x and z) xor (y and z)

private inline fun Sum0(x: Int): Int =
    ((x ushr 2) or (x shl 30)) xor ((x ushr 13) or (x shl 19)) xor ((x ushr 22) or (x shl 10))

private inline fun Sum1(x: Int): Int =
    ((x ushr 6) or (x shl 26)) xor ((x ushr 11) or (x shl 21)) xor ((x ushr 25) or (x shl 7))

private inline fun Theta0(x: Int): Int =
    ((x ushr 7) or (x shl 25)) xor ((x ushr 18) or (x shl 14)) xor (x ushr 3)

private inline fun Theta1(x: Int): Int =
    ((x ushr 17) or (x shl 15)) xor ((x ushr 19) or (x shl 13)) xor (x ushr 10)

/**
 * base implementation of MD4 family style digest as outlined in "Handbook of Applied Cryptography",
 * pages 344 - 347.
 */
public abstract class GeneralDigest : Digest {
    private val xBuf = ByteArray(4)
    private var xBuffOff = 0
    private var byteCount = 0L

    public fun updateByte(byte: Byte): GeneralDigest = apply {
        xBuf[xBuffOff++] = byte
        if (xBuffOff == xBuf.size) {
            processWord(xBuf, 0)
            xBuffOff = 0
        }
        byteCount++
    }

    override fun update(source: ByteArray, startIndex: Int, endIndex: Int): GeneralDigest = apply {
        val length = endIndex - startIndex
        // fill the current word
        var i = 0
        if (xBuffOff != 0) {
            while (i < length) {
                xBuf[xBuffOff++] = source[startIndex + i++]
                if (xBuffOff == 4) {
                    processWord(xBuf, 0)
                    xBuffOff = 0
                    break
                }
            }
        }

        // process whole words.
        val limit = ((length - i) and 3.inv()) + i
        while (i < limit) {
            processWord(source, startIndex + i)
            i += 4
        }

        // load in the remainder.
        while (i < length) {
            xBuf[xBuffOff++] = source[startIndex + i++]
        }

        byteCount += length
    }

    public fun finish() {
        val bitLength = byteCount shl 3

        // add the pad bytes.
        updateByte(128.toByte())

        while (xBuffOff != 0) {
            updateByte(0.toByte())
        }
        processLength(bitLength)
        processBlock()
    }

    override fun reset() {
        byteCount = 0
        xBuffOff = 0
        xBuf.fill(0)
    }

    protected abstract fun processWord(input: ByteArray, offset: Int)
    protected abstract fun processLength(bitLength: Long)
    protected abstract fun processBlock()
}
