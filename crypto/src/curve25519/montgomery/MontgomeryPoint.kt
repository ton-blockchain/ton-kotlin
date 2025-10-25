@file:Suppress("OPT_IN_USAGE")

package org.ton.sdk.crypto.curve25519.montgomery

import org.ton.sdk.crypto.curve25519.edwards.CompressedEdwardsY
import org.ton.sdk.crypto.curve25519.edwards.EdwardsPoint
import org.ton.sdk.crypto.curve25519.field.FieldElement
import org.ton.sdk.crypto.curve25519.scalar.Scalar
import kotlin.experimental.xor
import kotlin.jvm.JvmStatic

internal class MontgomeryPoint(
    val data: ByteArray = ByteArray(SIZE_BYTES)
) {
    constructor(ep: EdwardsPoint) : this() {
        from(ep, this)
    }

    fun toEdwards(sign: Int, output: EdwardsPoint = EdwardsPoint()): EdwardsPoint {
        // To decompress the Montgomery u coordinate to an
        // `EdwardsPoint`, we apply the birational map to obtain the
        // Edwards y coordinate, then do Edwards decompression.
        //
        // The birational map is y = (u-1)/(u+1).
        //
        // The exceptional points are the zeros of the denominator,
        // i.e., u = -1.
        //
        // But when u = -1, v^2 = u*(u^2+486662*u+1) = 486660.
        //
        // Since this is nonsquare mod p, u = -1 corresponds to a point
        // on the twist, not the curve, so we can reject it early.

        val u = FieldElement.fromBytes(data)
        if (u.inner.contentEquals(MINUS_ONE.inner)) throw IllegalStateException("Montgomery u-coordinate is on twist")

        val uMinusOne = FieldElement.sub(u, ONE)
        val uPlusOne = FieldElement.add(u, ONE)
        uPlusOne.invert(uPlusOne)
        val y = FieldElement.mul(uMinusOne, uPlusOne)
        val yCompressed = CompressedEdwardsY()
        y.toBytes(yCompressed.data)
        yCompressed.data[31] = yCompressed.data[31] xor (sign shl 7).toByte()
        output.set(yCompressed)
        return output
    }

    fun mul(point: MontgomeryPoint, scalar: Scalar) = mul(point, scalar, this)

    companion object {
        const val SIZE_BYTES = 32
        private val ONE = FieldElement.one()
        private val MINUS_ONE = FieldElement.minusOne()

        @JvmStatic
        fun from(
            ep: EdwardsPoint,
            output: MontgomeryPoint = MontgomeryPoint()
        ): MontgomeryPoint {
            val u = FieldElement.add(ep.z, ep.y)
            val w = FieldElement.sub(ep.z, ep.y)
            w.invert(w)
            u.mul(u, w)
            u.toBytes(output.data)
            return output
        }

        fun from(
            pp: MontgomeryProjectivePoint,
            output: MontgomeryPoint = MontgomeryPoint()
        ): MontgomeryPoint {
            val wInv = FieldElement.invert(pp.w)
            val u = FieldElement.mul(pp.u, wInv)
            u.toBytes(output.data)
            return output
        }

        fun mul(
            point: MontgomeryPoint,
            scalar: Scalar,
            output: MontgomeryPoint = MontgomeryPoint()
        ): MontgomeryPoint {
            val affineU = FieldElement.fromBytes(point.data)
            val x0 = MontgomeryProjectivePoint.identity()
            val x1 = MontgomeryProjectivePoint(
                u = FieldElement.fromBytes(point.data),
                w = FieldElement.one()
            )
            val bits = scalar.bits()
            for (i in 254 downTo 0) {
                val choise = (bits[i + 1].toInt() xor bits[i].toInt())
                x0.conditionalSwap(x1, choise)
                MontgomeryProjectivePoint.montgomeryDifferentialAddAndDouble(x0, x1, affineU)
            }
            x0.conditionalSwap(x1, bits[0].toInt())
            return from(x0, output)
        }
    }
}
