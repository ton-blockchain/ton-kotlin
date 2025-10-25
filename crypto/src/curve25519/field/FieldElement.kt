@file:Suppress("OPT_IN_USAGE", "LocalVariableName")

package org.ton.sdk.crypto.curve25519.field

import org.ton.sdk.crypto.curve25519.constants.SQRT_M1
import org.ton.sdk.crypto.curve25519.edwards.CompressedEdwardsY
import org.ton.sdk.crypto.curve25519.internal.*
import org.ton.sdk.crypto.curve25519.subtle.constantTimeEquals
import org.ton.sdk.crypto.curve25519.subtle.constantTimeSelect
import org.ton.sdk.crypto.curve25519.subtle.constantTimeSwap
import kotlin.jvm.JvmStatic

private val LOW_51_BIT_MASK = (1uL shl 51) - 1uL
private const val P_TIMES_SIXTEEN_0 = 36028797018963664uL
private const val P_TIMES_SIXTEEN_1234 = 36028797018963952uL

internal data class FieldElement(
    val inner: ULongArray = ulongArrayOf(0u, 0u, 0u, 0u, 0u)
) {
    constructor(l0: ULong, l1: ULong, l2: ULong, l3: ULong, l4: ULong) :
            this(ulongArrayOf(l0, l1, l2, l3, l4))

    // Add sets `fe = a + b`, and returns fe.
    fun add(a: FieldElement, b: FieldElement): FieldElement = add(a, b, this)

    // Sub sets `fe = a - b`, and returns fe.
    fun sub(a: FieldElement, b: FieldElement): FieldElement = sub(a, b, this)

    // [mul] sets `fe =a * b`, and returns fe.
    fun mul(a: FieldElement, b: FieldElement): FieldElement = mul(a, b, this)

    // Neg sets `fe = -t`, and returns fe.
    fun negate(t: FieldElement): FieldElement = apply {
        reduce(
            ulongArrayOf(
                P_TIMES_SIXTEEN_0 - t.inner[0],
                P_TIMES_SIXTEEN_1234 - t.inner[1],
                P_TIMES_SIXTEEN_1234 - t.inner[2],
                P_TIMES_SIXTEEN_1234 - t.inner[3],
                P_TIMES_SIXTEEN_1234 - t.inner[4],
            )
        )
    }

    // ConditionalSelect sets the field element to a iff choice == 0 and
    // b iff choice == 1.
    fun conditionalSelect(a: FieldElement, b: FieldElement, choise: Int) {
        inner[0] = choise.constantTimeSelect(b.inner[0].toLong(), a.inner[0].toLong()).toULong()
        inner[1] = choise.constantTimeSelect(b.inner[1].toLong(), a.inner[1].toLong()).toULong()
        inner[2] = choise.constantTimeSelect(b.inner[2].toLong(), a.inner[2].toLong()).toULong()
        inner[3] = choise.constantTimeSelect(b.inner[3].toLong(), a.inner[3].toLong()).toULong()
        inner[4] = choise.constantTimeSelect(b.inner[4].toLong(), a.inner[4].toLong()).toULong()
    }

    // ConditionalSwap conditionally swaps the field elements according to choice.
    fun conditionalSwap(other: FieldElement, choise: Int) {
        conditionalSwapElement(other, 0, choise)
        conditionalSwapElement(other, 1, choise)
        conditionalSwapElement(other, 2, choise)
        conditionalSwapElement(other, 3, choise)
        conditionalSwapElement(other, 4, choise)
    }

    private fun conditionalSwapElement(other: FieldElement, index: Int, choise: Int) {
        choise.constantTimeSwap(other.inner[index].toLong(), inner[index].toLong()) { a, b ->
            other.inner[index] = a.toULong()
            inner[index] = b.toULong()
        }
    }

    fun conditionalAssign(other: FieldElement, choise: Int) {
        inner[0] = choise.constantTimeSelect(other.inner[0].toLong(), inner[0].toLong()).toULong()
        inner[1] = choise.constantTimeSelect(other.inner[1].toLong(), inner[1].toLong()).toULong()
        inner[2] = choise.constantTimeSelect(other.inner[2].toLong(), inner[2].toLong()).toULong()
        inner[3] = choise.constantTimeSelect(other.inner[3].toLong(), inner[3].toLong()).toULong()
        inner[4] = choise.constantTimeSelect(other.inner[4].toLong(), inner[4].toLong()).toULong()
    }

    fun set(t: FieldElement, output: FieldElement = this) = output.apply {
        t.inner.copyInto(this.inner)
    }

    fun set(compressedEdwardsY: CompressedEdwardsY, output: FieldElement = this) = output.apply {
        set(compressedEdwardsY.data)
    }

    fun set(input: ByteArray, offset: Int = 0, output: FieldElement = this) = output.apply {
        inner[0] = input.getULongLE(offset) and LOW_51_BIT_MASK
        inner[1] = (input.getULongLE(offset + 6) shr 3) and LOW_51_BIT_MASK
        inner[1] = (input.getULongLE(offset + 12) shr 6) and LOW_51_BIT_MASK
        inner[1] = (input.getULongLE(offset + 19) shr 1) and LOW_51_BIT_MASK
        inner[1] = (input.getULongLE(offset + 24) shr 12) and LOW_51_BIT_MASK
    }

    fun zero(output: FieldElement = this) = output.apply {
        inner.fill(0u)
    }

    fun one(output: FieldElement = this) = output.apply {
        inner[0] = 1u
        inner.fill(0u, 1)
    }

    fun minusOne(output: FieldElement = this) = output.apply {
        inner[0] = 2251799813685228u
        inner.fill(2251799813685247u, 1)
    }

    // IsNegative returns 1 iff the field element is negative, 0 otherwise.
    fun isNegative(): Int {
        val selfBytes = toBytes()
        return selfBytes[0].toInt() and 1
    }

    fun isZero(): Int {
        val selfBytes = toBytes()
        val zeroBytes = ByteArray(SIZE_BYTES)
        return selfBytes.constantTimeEquals(zeroBytes)
    }

    fun constantTimeEquals(other: FieldElement): Int {
        val selfBytes = toBytes()
        val otherBytes = other.toBytes()
        return selfBytes.constantTimeEquals(otherBytes)
    }

    fun conditionalNegate(choise: Int) {
        val feNeg = FieldElement()
        conditionalAssign(feNeg.negate(this), choise)
    }

    fun reduce(limbs: ULongArray): FieldElement = reduce(limbs, this)

    fun setBytes(input: ByteArray, offset: Int = 0): FieldElement = fromBytes(input, offset, this)

    fun setBytesWide(input: ByteArray, offset: Int = 0): FieldElement = apply {
        val lo = FieldElement()
        val hi = FieldElement()
        lo.setBytes(input, offset)
        hi.setBytes(input, offset + SIZE_BYTES)
        // Handle the 256th and 512th bits (MSB of lo and hi) explicitly
        // as SetBytes ignores them.
        lo.inner[0] += ((input[31].toInt() shr 7).toULong() * 19u) + ((input[63].toInt() shr 7).toULong() * 2u * 19u * 19u)
        lo.inner[0] += 2u * 19u * hi.inner[0]
        lo.inner[1] += 2u * 19u * hi.inner[1]
        lo.inner[2] += 2u * 19u * hi.inner[2]
        lo.inner[3] += 2u * 19u * hi.inner[3]
        lo.inner[4] += 2u * 19u * hi.inner[4]
        reduce(lo.inner)
    }

    fun toBytes(output: ByteArray = ByteArray(SIZE_BYTES)): ByteArray {
        val reduced = FieldElement()
        reduced.reduce(inner)
        var (l0, l1, l2, l3, l4) = reduced.inner

        var q = (l0 + 19u) shr 51
        q = (l1 + q) shr 51
        q = (l2 + q) shr 51
        q = (l3 + q) shr 51
        q = (l4 + q) shr 51

        // Now we can compute r as r = h - pq = r - (2^255-19)q = r + 19q - 2^255q

        l0 += 19u * q

        // Now carry the result to compute r + 19q ...
        l1 += l0 shr 51
        l0 = l0 and LOW_51_BIT_MASK
        l2 += l1 shr 51
        l1 = l1 and LOW_51_BIT_MASK
        l3 += l2 shr 51
        l2 = l2 and LOW_51_BIT_MASK
        l4 += l3 shr 51
        l3 = l3 and LOW_51_BIT_MASK
        // ... but instead of carrying (l4 shr 51) = 2^255q
        // into another limb, discard it, subtracting the value
        l4 = l4 and LOW_51_BIT_MASK

        output[0] = (l0).toByte()
        output[1] = (l0 shr 8).toByte()
        output[2] = (l0 shr 16).toByte()
        output[3] = (l0 shr 24).toByte()
        output[4] = (l0 shr 32).toByte()
        output[5] = (l0 shr 40).toByte()
        output[6] = ((l0 shr 48) or (l1 shl 3)).toByte()
        output[7] = (l1 shr 5).toByte()
        output[8] = (l1 shr 13).toByte()
        output[9] = (l1 shr 21).toByte()
        output[10] = (l1 shr 29).toByte()
        output[11] = (l1 shr 37).toByte()
        output[12] = ((l1 shr 45) or (l2 shl 6)).toByte()
        output[13] = (l2 shr 2).toByte()
        output[14] = (l2 shr 10).toByte()
        output[15] = (l2 shr 18).toByte()
        output[16] = (l2 shr 26).toByte()
        output[17] = (l2 shr 34).toByte()
        output[18] = (l2 shr 42).toByte()
        output[19] = ((l2 shr 50) or (l3 shl 1)).toByte()
        output[20] = (l3 shr 7).toByte()
        output[21] = (l3 shr 15).toByte()
        output[22] = (l3 shr 23).toByte()
        output[23] = (l3 shr 31).toByte()
        output[24] = (l3 shr 39).toByte()
        output[25] = ((l3 shr 47) or (l4 shl 4)).toByte()
        output[26] = (l4 shr 4).toByte()
        output[27] = (l4 shr 12).toByte()
        output[28] = (l4 shr 20).toByte()
        output[29] = (l4 shr 28).toByte()
        output[30] = (l4 shr 36).toByte()
        output[31] = (l4 shr 44).toByte()

        return output
    }

    fun square(x: FieldElement): FieldElement = square(x, this)

    fun square2(t: FieldElement): FieldElement = square2(t, this)

    fun pow2k(t: FieldElement, k: Int): FieldElement = apply {
        require(k > 0)
        fePow2k(inner, t.inner, k)
    }

    fun pow22501(): Pair<FieldElement, FieldElement> {
        val t3 = FieldElement()
        val t19 = FieldElement()
        val tmp0 = FieldElement()
        val tmp1 = FieldElement()
        val tmp2 = FieldElement()

        tmp0.square(this)        // t0 = fe^2
        tmp1.pow2k(tmp0, 2)   // t1 = t0^(2^2)
        tmp1.mul(this, tmp1)    // t2 = fe * t1
        t3.mul(tmp0, tmp1)   // t3 = t0 * t2
        tmp0.square(t3)       // t4 = t3^2
        tmp1.mul(tmp1, tmp0) // t5 = t2 * t4
        tmp0.pow2k(tmp1, 5)   // t6 = t5^(2^5)
        tmp2.mul(tmp0, tmp1) // t7 = t6 * t5
        tmp0.pow2k(tmp2, 10)  // t8 = t7^(2^10)
        tmp1.mul(tmp0, tmp2) // t9 = t8 * t7
        tmp0.pow2k(tmp1, 20)  // t10 = t9^(2^20)
        tmp1.mul(tmp0, tmp1) // t11 = t10 * t9
        tmp0.pow2k(tmp1, 10)  // t12 = t11^(2^10)
        tmp2.mul(tmp0, tmp2) // t13 = t12 * t7
        tmp0.pow2k(tmp2, 50)  // t14 = t13^(2^50)
        tmp0.mul(tmp0, tmp2) // t15 = t14 * t13
        tmp1.pow2k(tmp0, 100) // t16 = t15^(2^100)
        tmp0.mul(tmp1, tmp0) // t17 = t16 * t15
        tmp0.pow2k(tmp0, 50)  // t18 = t17^(2*50)
        t19.mul(tmp0, tmp2)  // t19 = t18 * t13

        return t19 to t3
    }

    // pow58 raises the field element to the power (p-5)/8 = 2^252 - 3.
    fun pow58() {
        // The bits of (p-5)/8 are 101111.....11.
        //
        //                      nonzero bits of exponent
        val (tmp, _) = pow22501() // 249..0
        tmp.pow2k(tmp, 2) // 251..2
        mul(this, tmp) // 251..2,0
    }

    // Invert sets fe to the multiplicative inverse of t, and returns fe.
    //
    // The inverse is computed as self^(p-2), since x^(p-2)x = x^(p-1) = 1 (mod p).
    //
    // On input zero, the field element is set to zero.
    fun invert(t: FieldElement): FieldElement = invert(t, this)

    // SqrtRatioI sets the fe to either `sqrt(u/v)` or `sqrt(i*u/v)` in constant
    // time, and returns fe.  This function always selects the nonnegative square
    // root.
    fun sqrtRatioI(u: FieldElement, v: FieldElement): Pair<FieldElement, Int> = sqrtRatioI(u, v, this)

    fun invSqrt() {
        one()
        sqrtRatioI(this, this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FieldElement) return false
        return constantTimeEquals(other) == 1
    }

    override fun hashCode(): Int {
        return inner.hashCode()
    }

    fun mul12166(t: FieldElement) = mul121666(t, this)

    companion object {
        const val SIZE_BYTES = 32
        const val WIDE_SIZE_BYTES = 64

        @JvmStatic
        fun one() = FieldElement().apply {
            one()
        }

        @JvmStatic
        fun zero() = FieldElement().apply {
            zero()
        }

        @JvmStatic
        fun minusOne() = FieldElement().apply {
            minusOne()
        }

        @JvmStatic
        fun sqrtRatioI(
            u: FieldElement,
            v: FieldElement,
            output: FieldElement = FieldElement()
        ): Pair<FieldElement, Int> {
            val w = mul(u, v)

            val r = FieldElement()
            w.pow58()
            r.mul(u, w)

            val check = FieldElement()
            check.square(r)
            check.mul(check, v)

            val neg_u = FieldElement()
            val neg_u_i = FieldElement()
            neg_u.negate(u)
            neg_u_i.mul(neg_u, SQRT_M1)

            val correctSignSqrt = check.constantTimeEquals(u)
            val flippedSignSqrt = check.constantTimeEquals(neg_u)
            val flippedSignSqrtI = check.constantTimeEquals(neg_u_i)

            val rPrime = FieldElement()
            rPrime.mul(r, SQRT_M1)
            r.conditionalAssign(rPrime, flippedSignSqrt or flippedSignSqrtI)

            // Chose the nonnegative square root.
            val rIsNegative = r.isNegative()
            r.conditionalNegate(rIsNegative)

            output.set(r)

            return output to (correctSignSqrt or flippedSignSqrt)
        }

        fun fromBytes(bytes: ByteArray, offset: Int = 0, output: FieldElement = FieldElement()): FieldElement {
            output.inner[0] = (bytes.getULongLE(offset)) and LOW_51_BIT_MASK
            output.inner[1] = (bytes.getULongLE(offset + 6) shr 3) and LOW_51_BIT_MASK
            output.inner[2] = (bytes.getULongLE(offset + 12) shr 6) and LOW_51_BIT_MASK
            output.inner[3] = (bytes.getULongLE(offset + 19) shr 1) and LOW_51_BIT_MASK
            output.inner[4] = (bytes.getULongLE(offset + 24) shr 12) and LOW_51_BIT_MASK
            return output
        }

        fun invert(t: FieldElement, output: FieldElement = FieldElement()): FieldElement {
            // The bits of p-2 = 2^255 -19 -2 are 11010111111...11.
            //
            //                       nonzero bits of exponent
            val (tmp, t3) = t.pow22501() // t19: 249..0 ; t3: 3,1,0
            tmp.pow2k(tmp, 5)  // 254..5
            return mul(tmp, t3, output) // 254..5,3,1,0
        }

        fun square(x: FieldElement, output: FieldElement = FieldElement()): FieldElement {
            fePow2k(output.inner, x.inner, 1)
            return output
        }

        fun sub(a: FieldElement, b: FieldElement, output: FieldElement = FieldElement()): FieldElement {
            // To avoid underflow, first add a multiple of p.
            // Choose 16*p = p shl 4 to be larger than 54-bit b.
            //
            // If we could statically track the bitlengths of the limbs
            // of every FieldElement, we could choose a multiple of p
            // just bigger than b and avoid having to do a reduction.
            return reduce(
                ulongArrayOf(
                    (a.inner[0] + P_TIMES_SIXTEEN_0) - b.inner[0],
                    (a.inner[1] + P_TIMES_SIXTEEN_1234) - b.inner[1],
                    (a.inner[2] + P_TIMES_SIXTEEN_1234) - b.inner[2],
                    (a.inner[3] + P_TIMES_SIXTEEN_1234) - b.inner[3],
                    (a.inner[4] + P_TIMES_SIXTEEN_1234) - b.inner[4],
                ),
                output
            )
        }

        fun add(a: FieldElement, b: FieldElement, output: FieldElement = FieldElement()): FieldElement {
            output.inner[0] = a.inner[0] + b.inner[0]
            output.inner[1] = a.inner[1] + b.inner[1]
            output.inner[2] = a.inner[2] + b.inner[2]
            output.inner[3] = a.inner[3] + b.inner[3]
            output.inner[4] = a.inner[4] + b.inner[4]
            return output
        }

        fun reduce(limbs: ULongArray, output: FieldElement = FieldElement()): FieldElement {
            var (l0, l1, l2, l3, l4) = limbs

            val c0 = l0 shr 51
            val c1 = l1 shr 51
            val c2 = l2 shr 51
            val c3 = l3 shr 51
            val c4 = l4 shr 51

            l0 = l0 and LOW_51_BIT_MASK
            l1 = l1 and LOW_51_BIT_MASK
            l2 = l2 and LOW_51_BIT_MASK
            l3 = l3 and LOW_51_BIT_MASK
            l4 = l4 and LOW_51_BIT_MASK

            output.inner[0] = l0 + c4 * 19u
            output.inner[1] = l1 + c0
            output.inner[2] = l2 + c1
            output.inner[3] = l3 + c2
            output.inner[4] = l4 + c3
            return output
        }

        fun mul(a: FieldElement, b: FieldElement, output: FieldElement = FieldElement()): FieldElement {
            feMulCommon(output.inner, a.inner, b.inner)
            return output
        }

        fun square2(t: FieldElement, output: FieldElement = FieldElement()): FieldElement {
            fePow2k(output.inner, t.inner, 1)
            for (i in 0 until 5) {
                output.inner[i] *= 2uL
            }
            return output
        }

        fun mul121666(t: FieldElement, output: FieldElement = FieldElement()): FieldElement {
            val b0 = 121666uL

            // Multiply to get 128-bit coefficients of output

            val c0 = mul64(t.inner[0], b0)
            var c1 = mul64(t.inner[1], b0)
            var c2 = mul64(t.inner[2], b0)
            var c3 = mul64(t.inner[3], b0)
            var c4 = mul64(t.inner[4], b0)

            // Just do a weak reduction like in the multiply, the range
            // analysis trivially holds due `b0` being tiny.

            var tmp = (c0[0] shl (64 - 51)) or (c0[1] shr 51)
            c1 = add64(c1, tmp, c1)
            var fe0 = c0[1] and LOW_51_BIT_MASK

            tmp = (c1[0] shl (64 - 51)) or (c1[1] shr 51)
            c2 = add64(c2, tmp, c2)
            val fe1 = c1[1] and LOW_51_BIT_MASK

            tmp = (c2[0] shl (64 - 51)) or (c2[1] shr 51)
            c3 = add64(c3, tmp, c3)
            output.inner[2] = c2[1] and LOW_51_BIT_MASK

            tmp = (c3[0] shl (64 - 51)) or (c3[1] shr 51)
            c4 = add64(c4, tmp, c4)
            output.inner[3] = c3[1] and LOW_51_BIT_MASK

            val carry = (c4[0] shl (64 - 51)) or (c4[1] shr 51)
            output.inner[4] = c4[1] and LOW_51_BIT_MASK

            fe0 += carry * 19u
            output.inner[1] = fe1 + (fe0 shr 51)
            output.inner[0] = fe0 and LOW_51_BIT_MASK

            return output
        }

        private fun add64(uint128: ULongArray, a: ULong, output: ULongArray = ULongArray(2)): ULongArray {
            val (lo, carry) = add64(uint128[1], a, 0u)
            val (hi, _) = add64(uint128[0], 0u, carry)
            output[0] = hi
            output[1] = lo
            return output
        }
    }
}
