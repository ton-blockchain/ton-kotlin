@file:Suppress("OPT_IN_USAGE", "LocalVariableName")

package org.ton.kotlin.crypto.curve25519.internal

import org.ton.kotlin.crypto.curve25519.constants.LOW_51_BIT_NASK

private inline val ULongArray.lo get() = get(0)
private inline val ULongArray.hi get() = get(1)

private fun mul64(a: ULong, b: ULong): ULongArray {
    val uInt128 = ULongArray(2)
    val (hi, lo) = mul64(a, b, uInt128)
    uInt128[0] = lo
    uInt128[1] = hi
    return uInt128
}

private fun mulAdd64(uInt128: ULongArray, a: ULong, b: ULong) = uInt128.apply {
    val hi_0 = this.hi
    val lo_0 = this.lo
    val (hi_1, lo_1) = mul64(a, b, this)
    val (lo_2, c) = add64(lo_0, lo_1, 0u, this)
    val (hi_2, _) = add64(hi_0, hi_1, c, this)
    this[0] = lo_2
    this[1] = hi_2
}

private fun shift51(a: ULongArray, b: ULongArray): ULongArray = b.apply {
    val tmp = shiftRightBy51(a)
    val buf = ULongArray(2)
    val (lo, carry) = add64(b.lo, tmp, 0u, buf)
    val (hi, _) = add64(b.hi, 0u, carry, buf)
    b[0] = lo
    b[1] = hi
}

internal fun shiftRightBy51(uInt128: ULongArray): ULong {
    return (uInt128.hi shl (64 - 51)) or (uInt128.lo shr 51)
}

internal fun feMulCommon(v: ULongArray, a: ULongArray, b: ULongArray) {
    val a0 = a[0]
    val a1 = a[1]
    val a2 = a[2]
    val a3 = a[3]
    val a4 = a[4]

    val b0 = b[0]
    val b1 = b[1]
    val b2 = b[2]
    val b3 = b[3]
    val b4 = b[4]

    val a1_19 = a1 * 19u
    val a2_19 = a2 * 19u
    val a3_19 = a3 * 19u
    val a4_19 = a4 * 19u

    // r0 = a0×b0 + 19×(a1×b4 + a2×b3 + a3×b2 + a4×b1)
    var r0 = mul64(a0, b0)
    r0 = mulAdd64(r0, a1_19, b4)
    r0 = mulAdd64(r0, a2_19, b3)
    r0 = mulAdd64(r0, a3_19, b2)
    r0 = mulAdd64(r0, a4_19, b1)

    // r1 = a0×b1 + a1×b0 + 19×(a2×b4 + a3×b3 + a4×b2)
    var r1 = mul64(a0, b1)
    r1 = mulAdd64(r1, a1, b0)
    r1 = mulAdd64(r1, a2_19, b4)
    r1 = mulAdd64(r1, a3_19, b3)
    r1 = mulAdd64(r1, a4_19, b2)

    // r2 = a0×b2 + a1×b1 + a2×b0 + 19×(a3×b4 + a4×b3)
    var r2 = mul64(a0, b2)
    r2 = mulAdd64(r2, a1, b1)
    r2 = mulAdd64(r2, a2, b0)
    r2 = mulAdd64(r2, a3_19, b4)
    r2 = mulAdd64(r2, a4_19, b3)

    // r3 = a0×b3 + a1×b2 + a2×b1 + a3×b0 + 19×a4×b4
    var r3 = mul64(a0, b3)
    r3 = mulAdd64(r3, a1, b2)
    r3 = mulAdd64(r3, a2, b1)
    r3 = mulAdd64(r3, a3, b0)
    r3 = mulAdd64(r3, a4_19, b4)

    // r4 = a0×b4 + a1×b3 + a2×b2 + a3×b1 + a4×b0
    var r4 = mul64(a0, b4)
    r4 = mulAdd64(r4, a1, b3)
    r4 = mulAdd64(r4, a2, b2)
    r4 = mulAdd64(r4, a3, b1)
    r4 = mulAdd64(r4, a4, b0)

    // After the multiplication, we need to reduce (carry) the five coefficients
    // to obtain a result with limbs that are at most slightly larger than 2⁵¹,
    // to respect the Element invariant.
    //
    // Overall, the reduction works the same as carryPropagate, except with
    // wider inputs: we take the carry for each coefficient by shifting it right
    // by 51, and add it to the limb above it. The top carry is multiplied by 19
    // according to the reduction identity and added to the lowest limb.
    //
    // The largest coefficient (r0) will be at most 111 bits, which guarantees
    // that all carries are at most 111 - 51 = 60 bits, which fits in a uint64.
    //
    //     r0 = a0×b0 + 19×(a1×b4 + a2×b3 + a3×b2 + a4×b1)
    //     r0 < 2⁵²×2⁵² + 19×(2⁵²×2⁵² + 2⁵²×2⁵² + 2⁵²×2⁵² + 2⁵²×2⁵²)
    //     r0 < (1 + 19 × 4) × 2⁵² × 2⁵²
    //     r0 < 2⁷ × 2⁵² × 2⁵²
    //     r0 < 2¹¹¹
    //
    // Moreover, the top coefficient (r4) is at most 107 bits, so c4 is at most
    // 56 bits, and c4 * 19 is at most 61 bits, which again fits in a uint64 and
    // allows us to easily apply the reduction identity.
    //
    //     r4 = a0×b4 + a1×b3 + a2×b2 + a3×b1 + a4×b0
    //     r4 < 5 × 2⁵² × 2⁵²
    //     r4 < 2¹⁰⁷
    //

    var c0 = shiftRightBy51(r0)
    var c1 = shiftRightBy51(r1)
    var c2 = shiftRightBy51(r2)
    var c3 = shiftRightBy51(r3)
    var c4 = shiftRightBy51(r4)

    v[0] = (r0.lo and LOW_51_BIT_NASK) + c4 * 19uL
    v[1] = (r1.lo and LOW_51_BIT_NASK) + c0
    v[2] = (r2.lo and LOW_51_BIT_NASK) + c1
    v[3] = (r3.lo and LOW_51_BIT_NASK) + c2
    v[4] = (r4.lo and LOW_51_BIT_NASK) + c3

    // Now all coefficients fit into 64-bit registers but are still too large to
    // be passed around as a Element. We therefore do one last carry chain,
    // where the carries will be small enough to fit in the wiggle room above 2⁵¹.
    c0 = v[0] shr 51
    c1 = v[1] shr 51
    c2 = v[2] shr 51
    c3 = v[3] shr 51
    c4 = v[4] shr 51

    // c4 is at most 64 - 51 = 13 bits, so c4*19 is at most 18 bits, and
    // the final l0 will be at most 52 bits. Similarly, for the rest.
    v[0] = (v[0] and LOW_51_BIT_NASK) + c4 * 19u
    v[1] = (v[1] and LOW_51_BIT_NASK) + c0
    v[2] = (v[2] and LOW_51_BIT_NASK) + c1
    v[3] = (v[3] and LOW_51_BIT_NASK) + c2
    v[4] = (v[4] and LOW_51_BIT_NASK) + c3
}

internal fun fePow2k(fe: ULongArray, t: ULongArray, k: Int) {

    var a0 = t[0]
    var a1 = t[1]
    var a2 = t[2]
    var a3 = t[3]
    var a4 = t[4]

    repeat(k) {
        val a3_19 = a3 * 19u
        val a4_19 = a4 * 19u

        val d0 = a0 * 2u
        val d1 = a1 * 2u
        val d2 = a2 * 2u
        val d4 = a4 * 2u

        var r0 = mul64(a0, a0)
        r0 = mulAdd64(r0, d1, a4_19)
        r0 = mulAdd64(r0, d2, a3_19)

        var r1 = mul64(a3, a3_19)
        r1 = mulAdd64(r1, d0, a1)
        r1 = mulAdd64(r1, d2, a4_19)

        var r2 = mul64(a1, a1)
        r2 = mulAdd64(r2, d0, a2)
        r2 = mulAdd64(r2, d4, a3_19)

        var r3 = mul64(a4, a4_19)
        r3 = mulAdd64(r3, d0, a3)
        r3 = mulAdd64(r3, d1, a2)

        var r4 = mul64(a2, a2)
        r4 = mulAdd64(r4, d0, a4)
        r4 = mulAdd64(r4, d1, a3)

        a0 = r0.lo and LOW_51_BIT_NASK
        r1 = shift51(r0, r1)

        a1 = r1.lo and LOW_51_BIT_NASK
        r2 = shift51(r1, r2)

        a2 = r2.lo and LOW_51_BIT_NASK
        r3 = shift51(r2, r3)

        a3 = r3.lo and LOW_51_BIT_NASK
        r4 = shift51(r3, r4)

        a4 = r4.lo and LOW_51_BIT_NASK
        val carry = shiftRightBy51(r4)

        a0 += carry * 19u
        a1 += a0 shr 51
        a0 = a0 and LOW_51_BIT_NASK
    }
    fe[0] = a0
    fe[1] = a1
    fe[2] = a2
    fe[3] = a3
    fe[4] = a4
}
