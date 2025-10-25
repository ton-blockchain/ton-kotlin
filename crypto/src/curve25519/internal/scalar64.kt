@file:Suppress("OPT_IN_USAGE", "LocalVariableName", "NAME_SHADOWING")

package org.ton.sdk.crypto.curve25519.internal

import org.ton.sdk.crypto.curve25519.constants.L
import org.ton.sdk.crypto.curve25519.constants.LFACTOR
import org.ton.sdk.crypto.curve25519.constants.LOW_52_BIT_NASK
import org.ton.sdk.crypto.curve25519.constants.tables.AFFINE_ODD_MULTIPLES_OF_BASEPOINT
import org.ton.sdk.crypto.curve25519.constants.tables.ProjectiveNielsPointLookupTable
import org.ton.sdk.crypto.curve25519.constants.tables.ProjectiveNielsPointNafLookupTable
import org.ton.sdk.crypto.curve25519.edwards.EdwardsPoint
import org.ton.sdk.crypto.curve25519.models.CompletedPoint
import org.ton.sdk.crypto.curve25519.models.ProjectivePoint
import org.ton.sdk.crypto.curve25519.scalar.Scalar

private inline val ULongArray.hi get() = get(0)
private inline val ULongArray.lo get() = get(1)

internal fun varTimeDoubleScalarBaseMul(
    a: Scalar,
    A: EdwardsPoint,
    b: Scalar,
    output: EdwardsPoint = EdwardsPoint()
): EdwardsPoint {
    val tableA = ProjectiveNielsPointNafLookupTable.from(A)
    val aNaf = a.nonAdjacentForm(5)
    val bNaf = b.nonAdjacentForm(8)

    var i = 0
    for (j in 255 downTo 0) {
        if (aNaf[j].toInt() != 0 || bNaf[j].toInt() != 0) {
            i = j
            break
        }
    }

    val tableB = AFFINE_ODD_MULTIPLES_OF_BASEPOINT
    val r = ProjectivePoint.identity()

    val tEp = EdwardsPoint()
    val t = CompletedPoint()

    while (true) {
        t.double(r)

        if (aNaf[i] > 0) {
            t.add(tEp.set(t), tableA.lookup(aNaf[i]))
        } else if (aNaf[i] < 0) {
            t.sub(tEp.set(t), tableA.lookup((-aNaf[i]).toByte()))
        }

        if (bNaf[i] > 0) {
            t.add(tEp.set(t), tableB.lookup(bNaf[i]))
        } else if (bNaf[i] < 0) {
            t.sub(tEp.set(t), tableB.lookup((-bNaf[i]).toByte()))
        }

        r.set(t)

        if (i == 0) break
        i--
    }

    output.set(r)
    return output
}

internal fun edwardsMulCommon(point: EdwardsPoint, scalar: Scalar, output: EdwardsPoint): EdwardsPoint = output.apply {
    // Construct a lookup table of [P,2P,3P,4P,5P,6P,7P,8P]
    val lookupTable = ProjectiveNielsPointLookupTable.from(point)
    // Setting s = scalar, compute
    //
    //    s = s_0 + s_1*16^1 + ... + s_63*16^63,
    //
    // with `-8 <= s_i < 8` for `0 <= i < 63` and `-8 <= s_63 <= 8`.
    val scalarDigits = scalar.toRadix16()
    // Compute s*P as
    //
    //    s*P = P*(s_0 +   s_1*16^1 +   s_2*16^2 + ... +   s_63*16^63)
    //    s*P =  P*s_0 + P*s_1*16^1 + P*s_2*16^2 + ... + P*s_63*16^63
    //    s*P = P*s_0 + 16*(P*s_1 + 16*(P*s_2 + 16*( ... + P*s_63)...))
    //
    // We sum right-to-left.

    // Unwrap first loop iteration to save computing 16*identity
    val tmp3 = EdwardsPoint.identity()
    var multiple = lookupTable.lookup(scalarDigits[63])
    val tmp1 = CompletedPoint.add(tmp3, multiple)
    val tmp2 = ProjectivePoint()
    for (i in 62 downTo 0) {
        tmp2.set(tmp1)    // tmp2 =    (prev) in P2 coords
        tmp1.double(tmp2) // tmp1 =  2*(prev) in P1xP1 coords
        tmp2.set(tmp1)    // tmp2 =  2*(prev) in P2 coords
        tmp1.double(tmp2) // tmp1 =  4*(prev) in P1xP1 coords
        tmp2.set(tmp1)    // tmp2 =  4*(prev) in P2 coords
        tmp1.double(tmp2) // tmp1 =  8*(prev) in P1xP1 coords
        tmp2.set(tmp1)    // tmp2 =  8*(prev) in P2 coords
        tmp1.double(tmp2) // tmp1 = 16*(prev) in P1xP1 coords
        tmp3.set(tmp1)    // tmp3 = 16*(prev) in P3 coords
        multiple = lookupTable.lookup(scalarDigits[i])
        tmp1.add(tmp3, multiple)
        // Now tmp1 = s_i*P + 16*(prev) in P1xP1 coords
    }
    output.set(tmp1)
}

internal fun scalarMulInternal(a: ULongArray, b: ULongArray, output: ULongArray = ULongArray(18)): ULongArray =
    output.apply {
        val (z1, z0) = mul64(a[0], b[0])
        val (z3, z2) = add128(
            mul64(a[0], b[1]),
            mul64(a[1], b[0])
        )
        val (z5, z4) = add128(
            mul64(a[0], b[2]),
            add128(mul64(a[1], b[1]), mul64(a[2], b[0]))
        )
        val (z7, z6) = add128(
            add128(mul64(a[0], b[3]), mul64(a[1], b[2])),
            add128(mul64(a[2], b[1]), mul64(a[3], b[0]))
        )
        val (z9, z8) = add128(
            mul64(a[0], b[4]),
            add128(add128(mul64(a[1], b[3]), mul64(a[2], b[2])), add128(mul64(a[3], b[1]), mul64(a[4], b[0])))
        )
        val (z11, z10) = add128(
            add128(mul64(a[1], b[4]), mul64(a[2], b[3])),
            add128(mul64(a[3], b[2]), mul64(a[4], b[1]))
        )
        val (z13, z12) = add128(
            mul64(a[2], b[4]),
            add128(mul64(a[3], b[3]), mul64(a[4], b[2]))
        )
        val (z15, z14) = add128(
            mul64(a[3], b[4]),
            mul64(a[4], b[3])
        )
        val (z17, z16) = mul64(a[4], b[4])

        output[0] = z0; output[1] = z1
        output[2] = z2; output[3] = z3
        output[4] = z4; output[5] = z5
        output[6] = z6; output[7] = z7
        output[8] = z8; output[9] = z9
        output[10] = z10; output[11] = z11
        output[12] = z12; output[13] = z13
        output[14] = z14; output[15] = z15
        output[16] = z16; output[17] = z17
    }

internal fun scalarMontgomeryReduce(limbs: ULongArray, output: ULongArray = ULongArray(5)): ULongArray = output.apply {
    // note: l[3] is zero, so its multiples can be skipped
    // the first half computes the Montgomery adjustment factor n, and begins adding n*l to make limbs divisible by R
    val n0 = part1(limbs[1], limbs[0])
    val n1 = part1(
        add128(
            add128(n0, ulongArrayOf(limbs[3], limbs[2])),
            mul64(n0[2], L[1])
        )
    )
    val n2 = part1(
        add128(
            add128(n1, ulongArrayOf(limbs[5], limbs[4])),
            add128(mul64(n0[2], L[2]), mul64(n1[2], L[1]))
        )
    )
    val n3 = part1(
        add128(
            add128(n2, ulongArrayOf(limbs[7], limbs[6])),
            add128(mul64(n1[2], L[2]), mul64(n2[2], L[1]))
        )
    )
    val n4 = part1(
        add128(
            add128(n3, ulongArrayOf(limbs[9], limbs[8])),
            add128(mul64(n0[2], L[4]), add128(mul64(n2[2], L[2]), mul64(n3[2], L[1])))
        )
    )

    // limbs are divisible by R now, so we can divide by R by simply storing the upper half as the result

    val r0 = part2(
        add128(
            n4,
            add128(
                ulongArrayOf(limbs[11], limbs[10]),
                add128(mul64(n1[2], L[4]), add128(mul64(n3[2], L[2]), mul64(n4[2], L[1])))
            )
        )
    )
    val r1 = part2(
        add128(
            r0,
            add128(ulongArrayOf(limbs[13], limbs[12]), add128(mul64(n2[2], L[4]), mul64(n4[2], L[2])))
        )
    )
    val r2 = part2(
        add128(
            r1,
            add128(ulongArrayOf(limbs[15], limbs[14]), mul64(n3[2], L[4]))
        )
    )
    val (_, r4, r3) = part2(
        add128(
            r2,
            add128(ulongArrayOf(limbs[17], limbs[16]), mul64(n4[2], L[4]))
        )
    )
    output[0] = r0[2]
    output[1] = r1[2]
    output[2] = r2[2]
    output[3] = r3
    output[4] = r4
}

internal fun part1(a: ULong, b: ULong) = part1(ulongArrayOf(a, b))
internal fun part1(sum: ULongArray): ULongArray {
    val p = sum[1] * LFACTOR and LOW_52_BIT_NASK
    val t = mul64(p, L.data[0])
    var (hi, lo) = add128(sum, t)
    lo = (hi shl (64 - 52)) or (lo shr 52)
    hi = hi shr 52
    return ulongArrayOf(hi, lo, p)
}

internal fun part2(sum: ULongArray): ULongArray {
    var (hi, lo) = sum
    val w = lo and LOW_52_BIT_NASK
    lo = (hi shl (64 - 52)) or (lo shr 52)
    hi = hi shr 52
    return ulongArrayOf(hi, lo, w)
}
