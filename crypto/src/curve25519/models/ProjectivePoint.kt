package org.ton.kotlin.crypto.curve25519.models

import org.ton.kotlin.crypto.curve25519.edwards.EdwardsPoint
import org.ton.kotlin.crypto.curve25519.field.FieldElement
import kotlin.jvm.JvmStatic

internal data class ProjectivePoint(
    val x: FieldElement,
    val y: FieldElement,
    val z: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement(), FieldElement())

    fun identity(): ProjectivePoint = apply {
        identity(this)
    }

    fun set(cp: CompletedPoint): ProjectivePoint = apply {
        from(cp, this)
    }

    fun set(ep: EdwardsPoint): ProjectivePoint = apply {
        from(ep, this)
    }

    companion object {
        @JvmStatic
        fun identity(output: ProjectivePoint = ProjectivePoint()) = output.apply {
            output.x.zero()
            output.y.one()
            output.z.one()
        }

        @JvmStatic
        fun zero(output: ProjectivePoint = ProjectivePoint()) = output.apply {
            output.x.zero()
            output.y.one()
            output.z.one()
        }

        @JvmStatic
        fun from(cp: CompletedPoint, output: ProjectivePoint = ProjectivePoint()) = output.apply {
            output.x.mul(cp.x, cp.t)
            output.y.mul(cp.y, cp.z)
            output.z.mul(cp.z, cp.t)
        }

        @JvmStatic
        fun from(ep: EdwardsPoint, output: ProjectivePoint = ProjectivePoint()) = output.apply {
            output.x.set(ep.x)
            output.y.set(ep.y)
            output.z.set(ep.z)
        }
    }
}
