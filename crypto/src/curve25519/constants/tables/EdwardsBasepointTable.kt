@file:Suppress("OPT_IN_USAGE")

package org.ton.sdk.crypto.curve25519.constants.tables

import org.ton.sdk.crypto.curve25519.edwards.EdwardsPoint
import org.ton.sdk.crypto.curve25519.models.CompletedPoint
import org.ton.sdk.crypto.curve25519.scalar.Scalar

internal val ED25519_BASEPOINT_TABLE by lazy {
    EdwardsBasepointTable.unpack(PACKED_EDWARDS_BASEPOINT_TABLE)
}

internal class EdwardsBasepointTable(
    val data: Array<AffineNielsPointLookupTable> = Array(32) { AffineNielsPointLookupTable() }
) {
    operator fun get(index: Int) = data[index]

    fun mul(out: EdwardsPoint, scalar: Scalar): EdwardsPoint {
        val a = scalar.toRadix16()
        out.identity()
        val sum = CompletedPoint()
        for (i in 1 until 64 step 2) {
            val apt = get(i / 2).lookup(a[i])
            sum.add(out, apt)
            out.set(sum)
        }

        out.multByPow2(out, 4)

        for (i in 0 until 64 step 2) {
            val apt = get(i / 2).lookup(a[i])
            sum.add(out, apt)
            out.set(sum)
        }

        return out
    }

    companion object {
        fun unpack(packed: Array<ByteArray>): EdwardsBasepointTable {
            val tbl = EdwardsBasepointTable()

            for (tblIdx in 0 until 32) {
                for (i in 0 until 8) {
                    val packedIdx = tblIdx * 8 + i
                    tbl[tblIdx][i].setRawData(packed[packedIdx])
                }
            }

            return tbl
        }
    }
}
