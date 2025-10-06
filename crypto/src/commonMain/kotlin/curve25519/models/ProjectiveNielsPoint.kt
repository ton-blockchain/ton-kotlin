package org.ton.kotlin.crypto.curve25519.models

import org.ton.kotlin.crypto.curve25519.constants.EDWARDS_D2
import org.ton.kotlin.crypto.curve25519.edwards.EdwardsPoint
import org.ton.kotlin.crypto.curve25519.field.FieldElement
import kotlin.jvm.JvmStatic

internal data class ProjectiveNielsPoint(
    val yPlusX: FieldElement,
    val yMinusX: FieldElement,
    val z: FieldElement,
    val t2d: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement(), FieldElement())

    fun identity(): ProjectiveNielsPoint = identity(this)

    fun set(ep: EdwardsPoint): ProjectiveNielsPoint = from(ep, this)

    fun conditionalSelect(
        a: ProjectiveNielsPoint,
        b: ProjectiveNielsPoint,
        choise: Int
    ) {
        yPlusX.conditionalSelect(a.yPlusX, b.yPlusX, choise)
        yMinusX.conditionalSelect(a.yMinusX, b.yMinusX, choise)
        z.conditionalSelect(a.z, b.z, choise)
        t2d.conditionalSelect(a.t2d, b.t2d, choise)
    }

    fun conditionalAssign(
        other: ProjectiveNielsPoint,
        choise: Int
    ) {
        yPlusX.conditionalAssign(other.yPlusX, choise)
        yMinusX.conditionalAssign(other.yMinusX, choise)
        z.conditionalAssign(other.z, choise)
        t2d.conditionalAssign(other.t2d, choise)
    }

    fun conditionalNegate(
        choise: Int
    ) {
        yPlusX.conditionalSwap(yMinusX, choise)
        t2d.conditionalNegate(choise)
    }

    companion object {
        @JvmStatic
        fun identity(output: ProjectiveNielsPoint = ProjectiveNielsPoint()): ProjectiveNielsPoint {
            output.yPlusX.one()
            output.yMinusX.one()
            output.z.one()
            output.t2d.zero()
            return output
        }

        @JvmStatic
        fun from(ep: EdwardsPoint, output: ProjectiveNielsPoint = ProjectiveNielsPoint()): ProjectiveNielsPoint {
            output.yPlusX.add(ep.y, ep.x)
            output.yMinusX.sub(ep.y, ep.x)
            output.z.set(ep.z)
            output.t2d.mul(ep.t, EDWARDS_D2)
            return output
        }
    }
}
