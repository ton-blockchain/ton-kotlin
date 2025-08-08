import kotlin.random.Random

class Xoroshiro128PlusPlus(seed: Long) : Random() {
    private var state0: Long
    private var state1: Long

    init {
        val sm = SplitMix64(seed)
        state0 = sm.nextLong()
        state1 = sm.nextLong()
        require(state0 != 0L || state1 != 0L) { "Both states must not be zero" }
    }

    override fun nextBits(bitCount: Int): Int =
        (nextLong() ushr (64 - bitCount)).toInt()

    override fun nextBytes(array: ByteArray): ByteArray {
        for (i in array.indices) {
            array[i] = (nextInt()).toByte()
        }
        return array
    }

    override fun nextInt(): Int = nextLong().toInt()

    override fun nextLong(): Long {
        val s0 = state0
        val s1 = state1

        val result = (s0 + s1).rotateLeft(17) + s0

        val t = s1 xor s0
        state0 = s0.rotateLeft(49) xor t xor (t shl 21)
        state1 = t.rotateLeft(28)

        return result
    }

    private fun Long.rotateLeft(bits: Int): Long =
        (this shl bits) or (this ushr (64 - bits))

    private class SplitMix64(seed: Long) {
        private var state = seed
        fun nextLong(): Long {
            state += 0x9E3779B97F4A7C15uL.toLong()
            var z = state
            z = (z xor (z ushr 30)) * 0xBF58476D1CE4E5B9uL.toLong()
            z = (z xor (z ushr 27)) * 0x94D049BB133111EBuL.toLong()
            return z xor (z ushr 31)
        }
    }
}
