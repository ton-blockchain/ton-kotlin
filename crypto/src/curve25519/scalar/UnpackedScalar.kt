@file:Suppress("OPT_IN_USAGE")

package org.ton.sdk.crypto.curve25519.scalar

import org.ton.sdk.crypto.curve25519.constants.L
import org.ton.sdk.crypto.curve25519.constants.LOW_52_BIT_NASK
import org.ton.sdk.crypto.curve25519.constants.R
import org.ton.sdk.crypto.curve25519.constants.RR
import org.ton.sdk.crypto.curve25519.internal.getULongLE
import org.ton.sdk.crypto.curve25519.internal.scalarMontgomeryReduce
import org.ton.sdk.crypto.curve25519.internal.scalarMulInternal

internal class UnpackedScalar(
    val data: ULongArray = ULongArray(5)
) {
    constructor(l0: ULong, l1: ULong, l2: ULong, l3: ULong, l4: ULong) : this(ulongArrayOf(l0, l1, l2, l3, l4))

    operator fun get(index: Int) = data[index]

    fun bytes(input: ByteArray, offset: Int = 0) {
        val words = ULongArray(4) {
            input.getULongLE(offset + it * 8)
        }
        val topMask = (1uL shl 48) - 1uL

        data[0] = words[0] and LOW_52_BIT_NASK
        data[1] = ((words[0] shr 52) or (words[1] shl 12)) and LOW_52_BIT_NASK
        data[2] = ((words[1] shr 40) or (words[2] shl 24)) and LOW_52_BIT_NASK
        data[3] = ((words[2] shr 28) or (words[3] shl 36)) and LOW_52_BIT_NASK
        data[4] = (words[3] shr 16) and topMask
    }

    fun bytesWide(input: ByteArray, offset: Int = 0) {
        val words = ULongArray(8) {
            input.getULongLE(offset + it * 8)
        }
        val lo = UnpackedScalar(
            words[0] and LOW_52_BIT_NASK,
            ((words[0] shr 52) or ((words[1] shl 12))) and LOW_52_BIT_NASK,
            ((words[1] shr 40) or ((words[2] shl 24))) and LOW_52_BIT_NASK,
            ((words[2] shr 28) or ((words[3] shl 36))) and LOW_52_BIT_NASK,
            ((words[3] shr 16) or ((words[4] shl 48))) and LOW_52_BIT_NASK
        )
        val hi = UnpackedScalar(
            (words[4] shr 4) and LOW_52_BIT_NASK,
            ((words[4] shr 56) or (words[5] shl 8)) and LOW_52_BIT_NASK,
            ((words[5] shr 44) or (words[6] shl 20)) and LOW_52_BIT_NASK,
            ((words[6] shr 32) or (words[7] shl 32)) and LOW_52_BIT_NASK,
            words[7] shr 20
        )

        lo.montgomeryMul(lo, R) // (lo * R) / R = lo
        hi.montgomeryMul(hi, RR) // (hi * R^2) / R = hi * R

        // (hi * R) + lo
        add(hi, lo)
    }

    fun toByteArray(buf: ByteArray = ByteArray(Scalar.SIZE_BYTES), offset: Int = 0): ByteArray = buf.apply {
        buf[offset] = data[0].toByte()
        buf[offset + 1] = (data[0] shr 8).toByte()
        buf[offset + 2] = (data[0] shr 16).toByte()
        buf[offset + 3] = (data[0] shr 24).toByte()
        buf[offset + 4] = (data[0] shr 32).toByte()
        buf[offset + 5] = (data[0] shr 40).toByte()
        buf[offset + 6] = ((data[0] shr 48) or (data[1] shl 4)).toByte()
        buf[offset + 7] = (data[1] shr 4).toByte()
        buf[offset + 8] = (data[1] shr 12).toByte()
        buf[offset + 9] = (data[1] shr 20).toByte()
        buf[offset + 10] = (data[1] shr 28).toByte()
        buf[offset + 11] = (data[1] shr 36).toByte()
        buf[offset + 12] = (data[1] shr 44).toByte()
        buf[offset + 13] = (data[2]).toByte()
        buf[offset + 14] = (data[2] shr 8).toByte()
        buf[offset + 15] = (data[2] shr 16).toByte()
        buf[offset + 16] = (data[2] shr 24).toByte()
        buf[offset + 17] = (data[2] shr 32).toByte()
        buf[offset + 18] = (data[2] shr 40).toByte()
        buf[offset + 19] = ((data[2] shr 48) or (data[3] shl 4)).toByte()
        buf[offset + 20] = (data[3] shr 4).toByte()
        buf[offset + 21] = (data[3] shr 12).toByte()
        buf[offset + 22] = (data[3] shr 20).toByte()
        buf[offset + 23] = (data[3] shr 28).toByte()
        buf[offset + 24] = (data[3] shr 36).toByte()
        buf[offset + 25] = (data[3] shr 44).toByte()
        buf[offset + 26] = (data[4]).toByte()
        buf[offset + 27] = (data[4] shr 8).toByte()
        buf[offset + 28] = (data[4] shr 16).toByte()
        buf[offset + 29] = (data[4] shr 24).toByte()
        buf[offset + 30] = (data[4] shr 32).toByte()
        buf[offset + 31] = (data[4] shr 40).toByte()
    }

    /**
     * sets `s = a * b (mod l)`
     */
    fun mul(a: UnpackedScalar, b: UnpackedScalar) {
        var limbs = scalarMulInternal(a.data, b.data)
        scalarMontgomeryReduce(limbs, data)
        limbs = scalarMulInternal(data, RR.data)
        scalarMontgomeryReduce(limbs, data)
    }

    fun add(a: UnpackedScalar, b: UnpackedScalar) {
        // a + b
        var carry = 0uL
        repeat(5) {
            carry = a[it] + b[it] + (carry shr 52)
            data[it] = carry and LOW_52_BIT_NASK
        }

        // subtract l if the sum is >= l
        sub(this, L)
    }

    fun sub(a: UnpackedScalar, b: UnpackedScalar) {
        var borrow = 0UL
        repeat(5) {
            borrow = a.data[it] - (b.data[it] + (borrow shr 63))
            data[it] = borrow and LOW_52_BIT_NASK
        }

        val underflowMask = ((borrow shr 63) xor 1u) - 1u
        var carry = 0uL
        repeat(5) {
            carry = data[it] + (L.data[it] and underflowMask) + (carry shr 52)
            data[it] = carry and LOW_52_BIT_NASK
        }
    }

    fun montgomeryMul(a: UnpackedScalar, b: UnpackedScalar) {
        val limb = scalarMulInternal(a.data, b.data)
        val s = scalarMontgomeryReduce(limb)
        // result may be >= l, so attempt to subtract l
        sub(UnpackedScalar(s), L)
    }
}
