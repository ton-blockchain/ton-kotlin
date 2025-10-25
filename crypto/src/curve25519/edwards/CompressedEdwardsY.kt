package org.ton.sdk.crypto.curve25519.edwards

import org.ton.sdk.crypto.curve25519.constants.NON_CANONICAL_SIGN_BITS
import org.ton.sdk.crypto.curve25519.field.FieldElement
import kotlin.experimental.xor
import kotlin.jvm.JvmStatic

internal class CompressedEdwardsY(
    val data: ByteArray
) {
    constructor() : this(ByteArray(SIZE_BYTES))

    constructor(ep: EdwardsPoint) : this() {
        from(ep, this)
    }

    fun set(byteArray: ByteArray, offset: Int = 0) {
        byteArray.copyInto(data, 0, offset, offset + SIZE_BYTES)
    }

    fun set(point: EdwardsPoint): CompressedEdwardsY = from(point, this)

    fun isCannonicalVartime(): Boolean {
        if (!yCanonnical()) return false
        for (invalidEncodings in NON_CANONICAL_SIGN_BITS) {
            if (data.contentEquals(invalidEncodings.data)) return false
        }
        return true
    }

    private fun yCanonnical(): Boolean {
        if (data[0].toUInt() < 237u) return true
        for (i in 1 until 31) {
            if (data[i].toUInt() != 255u) return true
        }
        return (data[31].toUInt() or 128u) != 255u
    }

    companion object {
        const val SIZE_BYTES = 32

        @JvmStatic
        fun from(ep: EdwardsPoint, output: CompressedEdwardsY = CompressedEdwardsY()): CompressedEdwardsY {
            val x = FieldElement()
            val y = FieldElement()
            val recip = FieldElement()
            recip.invert(ep.z)
            x.mul(ep.x, recip)
            y.mul(ep.y, recip)

            y.toBytes(output.data)
            output.data[31] = output.data[31] xor (x.isNegative() shl 7).toByte()
            return output
        }
    }
}
