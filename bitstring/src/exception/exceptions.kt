package org.ton.kotlin.bitstring.exception

public class BitStringOverflowException(
    override val message: String? = "BitString overflow"
) : RuntimeException()

public class BitStringUnderflowException : RuntimeException("BitString underflow")
