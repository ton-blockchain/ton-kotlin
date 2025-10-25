package org.ton.sdk.crypto.curve25519.models

import org.ton.sdk.crypto.curve25519.constants.EDWARDS_D2
import org.ton.sdk.crypto.curve25519.edwards.EdwardsPoint
import org.ton.sdk.crypto.curve25519.field.FieldElement

internal data class AffineNielsPoint(
    val yPlusX: FieldElement,
    val yMinusX: FieldElement,
    val xy2d: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement())

    fun setRawData(rawData: ByteArray, offset: Int = 0) = apply {
        yPlusX.setBytes(rawData, offset)
        yMinusX.setBytes(rawData, offset + 32)
        xy2d.setBytes(rawData, offset + 64)
    }

    fun identity(): AffineNielsPoint = apply {
        yPlusX.one()
        yMinusX.one()
        xy2d.zero()
    }

    fun setEdwards(ep: EdwardsPoint): AffineNielsPoint = apply {
        val recip = FieldElement()
        val x = FieldElement()
        val y = FieldElement()
        val xy = FieldElement()
        recip.invert(ep.z)
        x.mul(ep.x, recip)
        y.mul(ep.y, recip)
        xy.mul(x, y)
        yPlusX.add(y, x)
        yMinusX.sub(y, x)
        xy2d.mul(xy, EDWARDS_D2)
    }

    fun conditionalSelect(
        a: AffineNielsPoint,
        b: AffineNielsPoint,
        choise: Int
    ) {
        yPlusX.conditionalSelect(a.yPlusX, b.yPlusX, choise)
        yMinusX.conditionalSelect(a.yMinusX, b.yMinusX, choise)
        xy2d.conditionalSelect(a.xy2d, b.xy2d, choise)
    }

    fun conditionalAssign(
        other: AffineNielsPoint,
        choise: Int
    ) {
        yPlusX.conditionalAssign(other.yPlusX, choise)
        yMinusX.conditionalAssign(other.yMinusX, choise)
        xy2d.conditionalAssign(other.xy2d, choise)
    }

    fun conditionalNegate(
        choise: Int
    ) {
        yPlusX.conditionalSwap(yMinusX, choise)
        xy2d.conditionalNegate(choise)
    }

    companion object
}
