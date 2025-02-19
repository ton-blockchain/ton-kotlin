package org.ton.kotlin.tvm.exception

public class UnknownOpcodeException(public val opcode: Int) :
    IllegalStateException("Unknown opcode: ${opcode.toHexString()}")
