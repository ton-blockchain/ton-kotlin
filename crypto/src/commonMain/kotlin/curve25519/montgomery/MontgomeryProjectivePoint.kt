@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.crypto.curve25519.montgomery

import org.ton.kotlin.crypto.curve25519.field.FieldElement
import kotlin.jvm.JvmStatic

internal data class MontgomeryProjectivePoint(
    val u: FieldElement,
    val w: FieldElement
) {
    constructor() : this(FieldElement(), FieldElement())

    fun identity() = identity(this)

    fun conditionalSwap(other: MontgomeryProjectivePoint, choise: Int) {
        u.conditionalSwap(other.u, choise)
        w.conditionalSwap(other.w, choise)
    }

    companion object {
        @JvmStatic
        fun identity(
            output: MontgomeryProjectivePoint = MontgomeryProjectivePoint()
        ): MontgomeryProjectivePoint {
            output.u.one()
            output.w.zero()
            return output
        }

        fun montgomeryDifferentialAddAndDouble(
            p: MontgomeryProjectivePoint,
            q: MontgomeryProjectivePoint,
            affinePmQ: FieldElement
        ) {
            val t0 = FieldElement.add(p.u, p.w)
            val t1 = FieldElement.sub(p.u, p.w)
            val t2 = FieldElement.add(q.u, q.w)
            val t3 = FieldElement.sub(q.u, q.w)

            val t4 = FieldElement.square(t0) // (U_P + W_P)^2 = U_P^2 + 2 U_P W_P + W_P^2
            val t5 = FieldElement.square(t1) // (U_P - W_P)^2 = U_P^2 - 2 U_P W_P + W_P^2

            val t6 = FieldElement.sub(t4, t5) // 4 U_P W_P

            val t7 = FieldElement.mul(t0, t3) // (U_P + W_P) (U_Q - W_Q) = U_P U_Q + W_P U_Q - U_P W_Q - W_P W_Q
            val t8 = FieldElement.mul(t1, t2) // (U_P - W_P) (U_Q + W_Q) = U_P U_Q - W_P U_Q + U_P W_Q - W_P W_Q

            // Note: dalek uses even more temporary variables, but eliminating them
            // is slightly faster since the Go compiler won't do that for us.

            q.u.add(t7, t8) // 2 (U_P U_Q - W_P W_Q): t9
            q.w.sub(t7, t8) // 2 (W_P U_Q - U_P W_Q): t10

            q.u.square(q.u) // 4 (U_P U_Q - W_P W_Q)^2: t11
            q.w.square(q.w) // 4 (W_P U_Q - U_P W_Q)^2: t12

            p.w.mul12166(t6) // (A + 2) U_P U_Q: t13

            p.u.mul(t4, t5) // ((U_P + W_P)(U_P - W_P))^2 = (U_P^2 - W_P^2)^2: t14
            p.w.add(p.w, t5) // (U_P - W_P)^2 + (A + 2) U_P W_P: t15

            p.w.mul(t6, p.w) // 4 (U_P W_P) ((U_P - W_P)^2 + (A + 2) U_P W_P): t16
            q.w.mul(affinePmQ, q.w) // U_D * 4 (W_P U_Q - U_P W_Q)^2: t17
            // t18 := t11             // W_D * 4 (U_P U_Q - W_P W_Q)^2: t18

            // P.U = t14 // U_{P'} = (U_P + W_P)^2 (U_P - W_P)^2
            // P.W = t16 // W_{P'} = (4 U_P W_P) ((U_P - W_P)^2 + ((A + 2)/4) 4 U_P W_P)
            // Q.U = t18 // U_{Q'} = W_D * 4 (U_P U_Q - W_P W_Q)^2
            // Q.W = t17 // W_{Q'} = U_D * 4 (W_P U_Q - U_P W_Q)^2
        }
    }
}
