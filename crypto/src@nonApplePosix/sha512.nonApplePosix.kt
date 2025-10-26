@file:Suppress("NOTHING_TO_INLINE")

package org.ton.sdk.crypto

public actual class Sha512 : LongDigest {
    public actual constructor() : super()

    init {
        reset()
    }

    actual override val digestSize: Int get() = SIZE_BYTES

    actual override val blockSize: Int get() = BLOCK_SIZE_BYTES

    actual override fun update(source: ByteArray, startIndex: Int, endIndex: Int): Sha512 = apply {
        super.update(source, startIndex, endIndex)
    }

    actual override fun digest(destination: ByteArray, destinationOffset: Int) {
        finish()
        destination.setLongAt(destinationOffset, h1)
        destination.setLongAt(destinationOffset + 8, h2)
        destination.setLongAt(destinationOffset + 16, h3)
        destination.setLongAt(destinationOffset + 24, h4)
        destination.setLongAt(destinationOffset + 32, h5)
        destination.setLongAt(destinationOffset + 40, h6)
        destination.setLongAt(destinationOffset + 48, h7)
        destination.setLongAt(destinationOffset + 56, h8)
        reset()
    }

    actual override fun reset() {
        super.reset()

        /*
        SHA-512 initial hash value
        The first 64 bits of the fractional parts of the square roots
        of the first eight prime numbers
        */
        h1 = 0x6a09e667f3bcc908L
        h2 = 0xbb67ae8584caa73buL.toLong()
        h3 = 0x3c6ef372fe94f82bL
        h4 = 0xa54ff53a5f1d36f1uL.toLong()
        h5 = 0x510e527fade682d1L
        h6 = 0x9b05688c2b3e6c1fuL.toLong()
        h7 = 0x1f83d9abfb41bd6bL
        h8 = 0x5be0cd19137e2179L
    }

    actual override fun close() {
        reset()
    }

    public companion object {
        public const val ALGORITHM_NAME: String = "SHA-512"
        public const val SIZE_BYTES: Int = 64
        public const val SIZE_BITS: Int = SIZE_BYTES * Byte.SIZE_BITS
        public const val BLOCK_SIZE_BYTES: Int = 128
        public const val BLOCK_SIZE_BITS: Int = BLOCK_SIZE_BYTES * Byte.SIZE_BITS

        private fun ByteArray.setLongAt(index: Int, value: Long) {
            this[index] = (value ushr 56).toByte()
            this[index + 1] = (value ushr 48).toByte()
            this[index + 2] = (value ushr 40).toByte()
            this[index + 3] = (value ushr 32).toByte()
            this[index + 4] = (value ushr 24).toByte()
            this[index + 5] = (value ushr 16).toByte()
            this[index + 6] = (value ushr 8).toByte()
            this[index + 7] = value.toByte()
        }
    }
}

/**
 * Base class for SHA-384 and SHA-512.
 */
public abstract class LongDigest : Digest {
    private val xBuf = ByteArray(8)
    private var xBufOff: Int = 0
    private var byteCount1: Long = 0
    private var byteCount2: Long = 0
    private val w = LongArray(80)
    private var wOff = 0

    protected var h1: Long = 0L
    protected var h2: Long = 0L
    protected var h3: Long = 0L
    protected var h4: Long = 0L
    protected var h5: Long = 0L
    protected var h6: Long = 0L
    protected var h7: Long = 0L
    protected var h8: Long = 0L

    public fun updateByte(byte: Byte): LongDigest = apply {
        xBuf[xBufOff++] = byte
        if (xBufOff == xBuf.size) {
            processWord(xBuf, 0)
            xBufOff = 0
        }
        byteCount1++
    }

    override fun update(source: ByteArray, startIndex: Int, endIndex: Int): LongDigest = apply {
        var inOff = startIndex
        var len = endIndex - startIndex

        while (xBufOff != 0 && len > 0) {
            updateByte(source[inOff])
            inOff++
            len--
        }

        while (len > xBuf.size) {
            processWord(source, inOff)
            inOff += xBuf.size
            len -= xBuf.size
            byteCount1 += xBuf.size.toLong()
        }

        while (len > 0) {
            updateByte(source[inOff])
            inOff++
            len--
        }
    }

    public fun finish() {
        adjustByteCounts()

        val lowBitLength = byteCount1 shl 3
        val hiBitLength = byteCount2

        updateByte(0x80.toByte())

        while (xBufOff != 0) {
            updateByte(0.toByte())
        }

        processLength(lowBitLength, hiBitLength)
        processBlock()
    }

    override fun reset() {
        byteCount1 = 0
        byteCount2 = 0
        xBufOff = 0
        xBuf.fill(0)
        wOff = 0
        w.fill(0)
    }

    protected fun processWord(input: ByteArray, inOff: Int) {
        var n = input[inOff].toLong() shl 56
        n = n or ((input[inOff + 1].toLong() and 0xFF) shl 48)
        n = n or ((input[inOff + 2].toLong() and 0xFF) shl 40)
        n = n or ((input[inOff + 3].toLong() and 0xFF) shl 32)
        n = n or ((input[inOff + 4].toLong() and 0xFF) shl 24)
        n = n or ((input[inOff + 5].toLong() and 0xFF) shl 16)
        n = n or ((input[inOff + 6].toLong() and 0xFF) shl 8)
        n = n or (input[inOff + 7].toLong() and 0xFF)
        w[wOff] = n
        if (++wOff == 16) {
            processBlock()
        }
    }

    protected fun adjustByteCounts() {
        if (byteCount1 > 0x1fffffffffffffffL) {
            byteCount2 += byteCount1 ushr 61
            byteCount1 = byteCount1 and 0x1fffffffffffffffL
        }
    }

    protected fun processLength(
        lowW: Long,
        hiW: Long
    ) {
        if (wOff > 14) {
            processBlock()
        }

        w[14] = hiW
        w[15] = lowW
    }

    protected fun processBlock() {
        adjustByteCounts()

        //
        // expand 16 word block into 80 word blocks.
        //
        for (t in 16 until 80) {
            w[t] = w[t - 2].sigma1() + w[t - 7] + w[t - 15].sigma0() + w[t - 16]
        }

        var a = h1
        var b = h2
        var c = h3
        var d = h4
        var e = h5
        var f = h6
        var g = h7
        var h = h8

        var t = 0
        for (i in 0 until 10) {
            // t = 8 * i
            h += Sum1(e) + ch(e, f, g) + k[t] + w[t++]
            d += h
            h += Sum0(a) + Maj(a, b, c)

            // t = 8 * i + 1
            g += Sum1(d) + ch(d, e, f) + k[t] + w[t++]
            c += g
            g += Sum0(h) + Maj(h, a, b)

            // t = 8 * i + 2
            f += Sum1(c) + ch(c, d, e) + k[t] + w[t++]
            b += f
            f += Sum0(g) + Maj(g, h, a)

            // t = 8 * i + 3
            e += Sum1(b) + ch(b, c, d) + k[t] + w[t++]
            a += e
            e += Sum0(f) + Maj(f, g, h)

            // t = 8 * i + 4
            d += Sum1(a) + ch(a, b, c) + k[t] + w[t++]
            h += d
            d += Sum0(e) + Maj(e, f, g)

            // t = 8 * i + 5
            c += Sum1(h) + ch(h, a, b) + k[t] + w[t++]
            g += c
            c += Sum0(d) + Maj(d, e, f)

            // t = 8 * i + 6
            b += Sum1(g) + ch(g, h, a) + k[t] + w[t++]
            f += b
            b += Sum0(c) + Maj(c, d, e)

            // t = 8 * i + 7
            a += Sum1(f) + ch(f, g, h) + k[t] + w[t++]
            e += a
            a += Sum0(b) + Maj(b, c, d)
        }

        h1 += a
        h2 += b
        h3 += c
        h4 += d
        h5 += e
        h6 += f
        h7 += g
        h8 += h

        wOff = 0
        w.fill(0, 0, 16)
    }
}

private inline fun ch(x: Long, y: Long, z: Long): Long = (x and y) xor (x.inv() and z)

private inline fun Maj(x: Long, y: Long, z: Long): Long = (x and y) or (x and z) or (y and z)

private inline fun Sum0(x: Long): Long =
    ((x shl 36) or (x ushr 28)) xor ((x shl 30) or (x ushr 34)) xor ((x shl 25) or (x ushr 39))

private inline fun Sum1(x: Long): Long =
    ((x shl 50) or (x ushr 14)) xor ((x shl 46) or (x ushr 18)) xor ((x shl 23) or (x ushr 41))

private inline fun Long.sigma0(): Long =
    ((this shl 63) or (this ushr 1)) xor ((this shl 56) or (this ushr 8)) xor (this ushr 7)

private inline fun Long.sigma1(): Long =
    ((this shl 45) or (this ushr 19)) xor ((this shl 3) or (this ushr 61)) xor (this ushr 6)

/* SHA-384 and SHA-512 Constants
 * (represent the first 64 bits of the fractional parts of the
 * cube roots of the first sixty-four prime numbers)
 */
private val k = ulongArrayOf(
    0x428a2f98d728ae22uL, 0x7137449123ef65cduL, 0xb5c0fbcfec4d3b2fuL, 0xe9b5dba58189dbbcuL,
    0x3956c25bf348b538uL, 0x59f111f1b605d019uL, 0x923f82a4af194f9buL, 0xab1c5ed5da6d8118uL,
    0xd807aa98a3030242uL, 0x12835b0145706fbeuL, 0x243185be4ee4b28cuL, 0x550c7dc3d5ffb4e2uL,
    0x72be5d74f27b896fuL, 0x80deb1fe3b1696b1uL, 0x9bdc06a725c71235uL, 0xc19bf174cf692694uL,
    0xe49b69c19ef14ad2uL, 0xefbe4786384f25e3uL, 0x0fc19dc68b8cd5b5uL, 0x240ca1cc77ac9c65uL,
    0x2de92c6f592b0275uL, 0x4a7484aa6ea6e483uL, 0x5cb0a9dcbd41fbd4uL, 0x76f988da831153b5uL,
    0x983e5152ee66dfabuL, 0xa831c66d2db43210uL, 0xb00327c898fb213fuL, 0xbf597fc7beef0ee4uL,
    0xc6e00bf33da88fc2uL, 0xd5a79147930aa725uL, 0x06ca6351e003826fuL, 0x142929670a0e6e70uL,
    0x27b70a8546d22ffcuL, 0x2e1b21385c26c926uL, 0x4d2c6dfc5ac42aeduL, 0x53380d139d95b3dfuL,
    0x650a73548baf63deuL, 0x766a0abb3c77b2a8uL, 0x81c2c92e47edaee6uL, 0x92722c851482353buL,
    0xa2bfe8a14cf10364uL, 0xa81a664bbc423001uL, 0xc24b8b70d0f89791uL, 0xc76c51a30654be30uL,
    0xd192e819d6ef5218uL, 0xd69906245565a910uL, 0xf40e35855771202auL, 0x106aa07032bbd1b8uL,
    0x19a4c116b8d2d0c8uL, 0x1e376c085141ab53uL, 0x2748774cdf8eeb99uL, 0x34b0bcb5e19b48a8uL,
    0x391c0cb3c5c95a63uL, 0x4ed8aa4ae3418acbuL, 0x5b9cca4f7763e373uL, 0x682e6ff3d6b2b8a3uL,
    0x748f82ee5defb2fcuL, 0x78a5636f43172f60uL, 0x84c87814a1f0ab72uL, 0x8cc702081a6439ecuL,
    0x90befffa23631e28uL, 0xa4506cebde82bde9uL, 0xbef9a3f7b2c67915uL, 0xc67178f2e372532buL,
    0xca273eceea26619cuL, 0xd186b8c721c0c207uL, 0xeada7dd6cde0eb1euL, 0xf57d4f7fee6ed178uL,
    0x06f067aa72176fbauL, 0x0a637dc5a2c898a6uL, 0x113f9804bef90daeuL, 0x1b710b35131c471buL,
    0x28db77f523047d84uL, 0x32caab7b40c72493uL, 0x3c9ebe0a15c9bebcuL, 0x431d67c49c100d4cuL,
    0x4cc5d4becb3e42b6uL, 0x597f299cfc657e2auL, 0x5fcb6fab3ad6faecuL, 0x6c44198c4a475817uL
).asLongArray()
