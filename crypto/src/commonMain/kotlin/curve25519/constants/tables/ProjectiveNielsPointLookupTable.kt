package org.ton.kotlin.crypto.curve25519.constants.tables

import org.ton.kotlin.crypto.curve25519.edwards.EdwardsPoint
import org.ton.kotlin.crypto.curve25519.models.CompletedPoint
import org.ton.kotlin.crypto.curve25519.models.ProjectiveNielsPoint
import org.ton.kotlin.crypto.curve25519.subtle.constantTimeEquals

internal class ProjectiveNielsPointLookupTable(
    val data: Array<ProjectiveNielsPoint>
) {
    fun lookup(x: Byte): ProjectiveNielsPoint {
        // Compute xabs = |x|
        val xmask = x.toInt() shr 7
        val xabs = ((x.toInt() + xmask) xor xmask).toByte()

        // Set t = 0 * P = identity
        val t = ProjectiveNielsPoint().identity()
        for (j in 1 until 9) {
            // Copy `points[j-1] == j*P` onto `t` in constant time if `|x| == j`.
            val c = xabs.constantTimeEquals(j.toByte())
            t.conditionalAssign(data[j - 1], c)
        }
        // Now t == |x| * P.

        val negMask = (xmask and 1).toByte().toInt()
        t.conditionalNegate(negMask)
        // Now t == x * P.
        return t
    }

    companion object {
        fun from(ep: EdwardsPoint): ProjectiveNielsPointLookupTable {
            val ai = Array(8) {
                ProjectiveNielsPoint.from(ep)
            }
            val a2 = EdwardsPoint.double(ep)

            for (i in 0 until 7) {
                val tmp = CompletedPoint()
                val tmp2 = EdwardsPoint()
                tmp.add(a2, ai[i])
                tmp2.set(tmp)
                ai[i + 1].set(tmp2)
            }

            return ProjectiveNielsPointLookupTable(ai)
        }
    }
}
