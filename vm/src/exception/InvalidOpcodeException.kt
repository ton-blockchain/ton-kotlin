package org.ton.kotlin.tvm.exception

public class InvalidOpcodeException(public val opcode: Int) :
    IllegalStateException("Unknown opcode: ${opcode.toHexString()}")
