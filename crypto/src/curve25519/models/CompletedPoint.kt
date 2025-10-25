package org.ton.sdk.crypto.curve25519.models

import org.ton.sdk.crypto.curve25519.edwards.EdwardsPoint
import org.ton.sdk.crypto.curve25519.field.FieldElement
import kotlin.jvm.JvmStatic

internal data class CompletedPoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement,
    val t: FieldElement,
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement(), FieldElement())

    fun add(a: EdwardsPoint, b: AffineNielsPoint) = add(a, b, this)
    fun add(a: EdwardsPoint, b: ProjectiveNielsPoint) = add(a, b, this)

    fun sub(a: EdwardsPoint, b: AffineNielsPoint) = sub(a, b, this)
    fun sub(a: EdwardsPoint, b: ProjectiveNielsPoint) = sub(a, b, this)

    fun double(pp: ProjectivePoint) = double(pp, this)

    companion object {
        @JvmStatic
        fun double(pp: ProjectivePoint, output: CompletedPoint = CompletedPoint()): CompletedPoint {
            val xx = FieldElement.square(pp.x)
            val yy = FieldElement.square(pp.y)
            val zz2 = FieldElement.square2(pp.z)
            val xPlusYsq = FieldElement.add(pp.x, pp.y)
            xPlusYsq.square(xPlusYsq)

            output.y.add(yy, xx)
            output.x.sub(xPlusYsq, output.y)
            output.z.sub(yy, xx)
            output.t.sub(zz2, output.z)

            return output
        }

        @JvmStatic
        fun add(a: EdwardsPoint, b: ProjectiveNielsPoint, output: CompletedPoint = CompletedPoint()): CompletedPoint {
            val pp = FieldElement.add(a.y, a.x)
            pp.mul(pp, b.yPlusX)
            val mm = FieldElement.sub(a.y, a.x)
            mm.mul(mm, b.yMinusX)
            val tt2d = FieldElement.mul(a.t, b.t2d)
            val zz = FieldElement.mul(a.z, b.z)
            val zz2 = FieldElement.add(zz, zz)

            output.x.sub(pp, mm)
            output.y.add(pp, mm)
            output.z.add(zz2, tt2d)
            output.t.sub(zz2, tt2d)
            return output
        }

        @JvmStatic
        fun sub(a: EdwardsPoint, b: ProjectiveNielsPoint, output: CompletedPoint = CompletedPoint()) = output.apply {
            val pm = FieldElement()
            val mp = FieldElement()
            val tt2d = FieldElement()
            val zz = FieldElement()
            val zz2 = FieldElement()
            pm.add(a.y, a.x)
            pm.mul(pm, b.yMinusX)
            mp.sub(a.y, a.x)
            mp.mul(mp, b.yPlusX)
            tt2d.mul(a.t, b.t2d)
            zz.mul(a.z, b.z)
            zz2.add(zz, zz)

            x.sub(pm, mp)
            y.add(pm, mp)
            z.sub(zz2, tt2d)
            t.add(zz2, tt2d)
        }

        @JvmStatic
        fun add(a: EdwardsPoint, b: AffineNielsPoint, output: CompletedPoint = CompletedPoint()): CompletedPoint {
            val pp = FieldElement().add(a.y, a.x)
            pp.mul(pp, b.yPlusX)
            val mm = FieldElement().sub(a.y, a.x)
            mm.mul(mm, b.yMinusX)
            val txy2d = FieldElement().mul(a.t, b.xy2d)
            val z2 = FieldElement().add(a.z, a.z)

            output.x.sub(pp, mm)
            output.y.add(pp, mm)
            output.z.add(z2, txy2d)
            output.t.sub(z2, txy2d)
            return output
        }

        @JvmStatic
        fun sub(a: EdwardsPoint, b: AffineNielsPoint, output: CompletedPoint = CompletedPoint()) = output.apply {
            val yPlusX = FieldElement()
            val yMinusX = FieldElement()
            val pm = FieldElement()
            val mp = FieldElement()
            val txy2d = FieldElement()
            val z2 = FieldElement()
            yPlusX.add(a.y, a.x)
            yMinusX.sub(a.y, a.x)
            pm.mul(yPlusX, b.yMinusX)
            mp.mul(yMinusX, b.yPlusX)
            txy2d.mul(a.t, b.xy2d)
            z2.add(a.z, a.z)

            x.sub(pm, mp)
            y.add(pm, mp)
            z.sub(z2, txy2d)
            t.add(z2, txy2d)
        }
    }
}
