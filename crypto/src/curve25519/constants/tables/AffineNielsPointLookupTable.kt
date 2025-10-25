package org.ton.sdk.crypto.curve25519.constants.tables

import org.ton.sdk.crypto.curve25519.models.AffineNielsPoint
import org.ton.sdk.crypto.curve25519.subtle.constantTimeEquals
import kotlin.jvm.JvmStatic

internal class AffineNielsPointLookupTable(
    val points: Array<AffineNielsPoint> = Array(64) { AffineNielsPoint() }
) {
    operator fun get(index: Int) = points[index]

    fun lookup(x: Byte, output: AffineNielsPoint = AffineNielsPoint()): AffineNielsPoint {
        // Compute xabs = |x|
        val xmask = x.toInt() ushr 7
        val xabs = ((x + xmask) xor xmask).toByte()

        // t == |x| * P
        lookupAffineNiels(output, xabs)

        // t == x * P.
        val negMask = (xmask and 1).toByte().toInt()
        output.conditionalNegate(negMask)

        return output
    }

    fun lookupAffineNiels(out: AffineNielsPoint, xabs: Byte): AffineNielsPoint {
        out.identity()
        for (j in 1 until 9) {
            // Copy `points[j-1] == j*P` onto `t` in constant time if `|x| == j`.
            val c = xabs.constantTimeEquals(j.toByte())
            out.conditionalAssign(get(j - 1), c)
        }
        return out
    }

    companion object {
        @JvmStatic
        fun unpack(packed: Array<ByteArray>): AffineNielsPointLookupTable {
            val table = AffineNielsPointLookupTable()
            packed.forEachIndexed { index, bytes ->
                table.points[index].setRawData(bytes)
            }
            return table
        }
    }
}
