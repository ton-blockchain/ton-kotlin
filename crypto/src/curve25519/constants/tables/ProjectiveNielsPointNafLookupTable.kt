package org.ton.sdk.crypto.curve25519.constants.tables

import org.ton.sdk.crypto.curve25519.edwards.EdwardsPoint
import org.ton.sdk.crypto.curve25519.models.CompletedPoint
import org.ton.sdk.crypto.curve25519.models.ProjectiveNielsPoint

internal class ProjectiveNielsPointNafLookupTable(
    val table: Array<ProjectiveNielsPoint> = Array(8) { ProjectiveNielsPoint() }
) {
    fun lookup(x: Byte) = table[x / 2]

    companion object {
        fun from(ep: EdwardsPoint): ProjectiveNielsPointNafLookupTable {
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

            return ProjectiveNielsPointNafLookupTable(ai)
        }
    }
}
