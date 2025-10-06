@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.crypto.curve25519.internal

private val MASK32 = (1uL shl 32) - 1uL

internal fun mul64(x: ULong, y: ULong, output: ULongArray = ULongArray(2)): ULongArray {
    val x0 = x and MASK32
    val x1 = x shr 32
    val y0 = y and MASK32
    val y1 = y shr 32
    val w0 = x0 * y0
    val t = x1 * y0 + (w0 shr 32)
    var w1 = t and MASK32
    val w2 = t shr 32
    w1 += x0 * y1
    output[0] = x1 * y1 + w2 + (w1 shr 32)
    output[1] = x * y
    return output
}

internal fun add64(x: ULong, y: ULong, carry: ULong, output: ULongArray = ULongArray(2)): ULongArray {
    output[0] = x + y + carry
    output[1] = ((x and y) or ((x or y) and output[0].inv())) shr 63
    return output
}

internal fun add128(x: ULongArray, y: ULongArray, output: ULongArray = ULongArray(2)): ULongArray {
    val (hi, carry) = add64(x[1], y[1], 0u, output)
    val (lo, _) = add64(x[0], y[0], carry, output)
    output[0] = lo
    output[1] = hi
    return output
}
