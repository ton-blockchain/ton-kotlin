package org.ton.kotlin.tvm.exception


public sealed class TvmException(override val message: String?) : RuntimeException()

public class IntegerOverflowTvmException() : TvmException("Integer overflow")

public class InvalidOpcodeException(public val opcode: Int) :
    TvmException("Unknown opcode: ${opcode.toHexString()}")

public class StackOverflowException : TvmException("Stack overflow")

public class StackUnderflowException : TvmException("Stack underflow")
