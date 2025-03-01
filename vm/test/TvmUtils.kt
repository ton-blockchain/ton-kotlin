package org.ton.kotlin.tvm

import org.ton.bitstring.BitString
import org.ton.cell.buildCell

fun runTvm(code: String, stack: Stack = stackOf()): Stack {
    Tvm().execute(stack, buildCell {
        storeBitString(BitString(code))
    })
    return stack
}
