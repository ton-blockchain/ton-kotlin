package org.ton.kotlin.crypto.curve25519.edwards

import org.ton.kotlin.crypto.curve25519.constants.EDWARDS_D
import org.ton.kotlin.crypto.curve25519.constants.tables.EdwardsBasepointTable
import org.ton.kotlin.crypto.curve25519.exceptioin.InvalidYCoordinateException
import org.ton.kotlin.crypto.curve25519.field.FieldElement
import org.ton.kotlin.crypto.curve25519.internal.edwardsMulCommon
import org.ton.kotlin.crypto.curve25519.models.AffineNielsPoint
import org.ton.kotlin.crypto.curve25519.models.CompletedPoint
import org.ton.kotlin.crypto.curve25519.models.ProjectiveNielsPoint
import org.ton.kotlin.crypto.curve25519.models.ProjectivePoint
import org.ton.kotlin.crypto.curve25519.scalar.Scalar
import kotlin.jvm.JvmStatic

internal data class EdwardsPoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement,
    val t: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement(), FieldElement())

    fun identity() = identity(this)

    fun set(pp: ProjectivePoint) = from(pp, this)

    fun set(ap: AffineNielsPoint) = from(ap, this)

    fun set(cp: CompletedPoint) = from(cp, this)

    fun double(t: EdwardsPoint) = double(t, this)

    @Throws(InvalidYCoordinateException::class)
    fun set(compressedY: CompressedEdwardsY) = from(compressedY, this)

    fun multByPow2(t: EdwardsPoint, k: Int) {
        val r = CompletedPoint()
        val s = ProjectivePoint()
        s.set(t)
        for (i in 0 until k - 1) {
            s.set(r.double(s))
        }
        // Unroll last iteration, so we can directly convert back to an EdwardsPoint.
        set(r.double(s))
    }

    fun mul(basepoint: EdwardsBasepointTable, scalar: Scalar): EdwardsPoint = mul(basepoint, scalar, this)

    fun mul(point: EdwardsPoint, scalar: Scalar) {
        edwardsMulCommon(point, scalar, this)
    }

    fun mulBasepoint(basepoint: EdwardsBasepointTable, scalar: Scalar) = basepoint.mul(this, scalar)

    fun negate(t: EdwardsPoint) = apply {
        negate(t, this)
    }

    fun isSmallOrder(): Boolean = mulByCofactor(this).isIdentity()

    fun isIdentity(): Boolean = constantTimeEquals(IDENTITY) == 1

    fun constantTimeEquals(other: EdwardsPoint): Int {
        // We would like to check that the point (X/Z, Y/Z) is equal to
        // the point (X'/Z', Y'/Z') without converting into affine
        // coordinates (x, y) and (x', y'), which requires two inversions.
        // We have that X = xZ and X' = x'Z'. Thus, x = x' is equivalent to
        // (xZ)Z' = (x'Z')Z, and similarly for the y-coordinate.

        val sXoz = FieldElement().mul(x, other.z)
        val oXsZ = FieldElement().mul(other.x, z)
        val sYoZ = FieldElement().mul(y, other.z)
        val oYsZ = FieldElement().mul(other.y, z)

        return sXoz.constantTimeEquals(oXsZ) and sYoZ.constantTimeEquals(oYsZ)
    }

    fun add(a: EdwardsPoint, b: EdwardsPoint) {
        val bpNiels = ProjectiveNielsPoint().apply {
            set(b)
        }
        val sum = CompletedPoint().apply {
            add(a, bpNiels)
        }
        set(sum)
    }

    companion object {
        private val IDENTITY = identity()

        @JvmStatic
        fun identity(output: EdwardsPoint = EdwardsPoint()): EdwardsPoint = output.apply {
            x.zero()
            y.one()
            z.one()
            t.zero()
        }

        @JvmStatic
        fun from(pp: ProjectivePoint, output: EdwardsPoint = EdwardsPoint()): EdwardsPoint = output.apply {
            x.mul(pp.x, pp.z)
            y.mul(pp.y, pp.z)
            z.square(pp.z)
            t.mul(pp.x, pp.y)
        }

        @JvmStatic
        fun from(ap: AffineNielsPoint, output: EdwardsPoint = EdwardsPoint()) = output.apply {
            identity(output)
            from(CompletedPoint.add(output, ap), output)
        }

        @JvmStatic
        fun from(cp: CompletedPoint, output: EdwardsPoint = EdwardsPoint()): EdwardsPoint {
            output.x.mul(cp.x, cp.t)
            output.y.mul(cp.y, cp.z)
            output.z.mul(cp.z, cp.t)
            output.t.mul(cp.x, cp.y)
            return output
        }

        @JvmStatic
        @Throws(InvalidYCoordinateException::class)
        fun from(compressedY: CompressedEdwardsY, output: EdwardsPoint = EdwardsPoint()): EdwardsPoint {
            val y = FieldElement.fromBytes(compressedY.data)
            val z = FieldElement.one()
            val yy = FieldElement.square(y)
            val u = FieldElement.sub(yy, z)
            val v = FieldElement.mul(yy, EDWARDS_D)
            v.add(v, z)
            val (x, isValidYCoord) = FieldElement.sqrtRatioI(u, v)
            require(isValidYCoord == 1) {
                "Invalid Y coordinate"
            }
            val compressedSignBit = ((compressedY.data[31].toInt() and 0xFF) ushr 7)
            x.conditionalNegate(compressedSignBit)

            output.x.set(x)
            output.y.set(y)
            output.z.set(z)
            output.t.mul(x, y)
            return output
        }

        @JvmStatic
        fun mul(point: EdwardsPoint, scalar: Scalar, output: EdwardsPoint = EdwardsPoint()) = output.apply {
            edwardsMulCommon(point, scalar, output)
        }

        @JvmStatic
        fun mul(basepoint: EdwardsBasepointTable, scalar: Scalar, output: EdwardsPoint = EdwardsPoint()): EdwardsPoint {
            return basepoint.mul(output, scalar)
        }

        @JvmStatic
        fun mulByPow2(t: EdwardsPoint, k: Int, output: EdwardsPoint = EdwardsPoint()) = output.apply {
            require(k > 0) { "k out of bounds" }
            val r = CompletedPoint()
            val s = ProjectivePoint.from(t)
            for (i in 0 until k - 1) {
                s.set(r.double(s))
            }
            output.set(r.double(s))
        }

        @JvmStatic
        fun mulByCofactor(t: EdwardsPoint, output: EdwardsPoint = EdwardsPoint()) = output.apply {
            mulByPow2(t, 3, output)
        }

        @JvmStatic
        fun negate(t: EdwardsPoint, output: EdwardsPoint = EdwardsPoint()) = output.apply {
            output.x.negate(t.x)
            output.y.set(t.y)
            output.z.set(t.z)
            output.t.negate(t.t)
        }

        @JvmStatic
        fun double(t: EdwardsPoint, output: EdwardsPoint = EdwardsPoint()): EdwardsPoint {
            return from(CompletedPoint.double(ProjectivePoint.from(t)), output)
        }
    }
}
