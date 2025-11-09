package utils

import kotlin.random.Random

class XorShift128Plus(
    private var seedA: Long,
    private var seedB: Long
) : Random() {
    constructor(seed: Long) : this(0, 0) {
        var seed = seed
        fun next(): Long {
            seed += 0x9E3779B97F4A7C15uL.toLong()
            var z = seed
            z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
            z = (z xor (z ushr 27)) * 0x94D049BB133111EBuL.toLong()
            return z xor (z ushr 31)
        }
        seedA = next()
        seedB = next()
    }

    override fun nextLong(): Long {
        var x = seedA
        val y = seedB
        seedA = y
        x = x xor (x shl 23)
        seedB = x xor y xor (x ushr 17) xor (y ushr 26)
        return seedB + y
    }

    override fun nextInt(): Int = nextLong().toInt()

    override fun nextLong(from: Long, until: Long): Long {
        val l = nextLong().toULong()
        val c = (until - from)
        val a = l % c.toULong()
        val b = a.toLong() + from
        return b
    }

    override fun nextInt(from: Int, until: Int): Int =  nextLong(from.toLong(), until.toLong()).toInt()

    override fun nextBits(bitCount: Int): Int = nextLong().toInt()

    override fun nextBoolean(): Boolean = nextInt(0, 2) == 1

    override fun nextBytes(array: ByteArray, fromIndex: Int, toIndex: Int): ByteArray {
        var cnt = 0
        var buf = 0L
        for (i in fromIndex until toIndex) {
            if (cnt == 0) {
                buf = nextLong()
                cnt = 8
            }
            array[i] = buf.toByte()
            buf = buf ushr 8
            cnt--
        }
        return array
    }
}
