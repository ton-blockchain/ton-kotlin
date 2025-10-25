package org.ton.sdk.bitstring

public interface BitStringBuilder {
    public fun storeBit(bit: Boolean)

    public fun storeByte(byte: Byte)

    public fun storeInt(int: Int)

    public fun storeShort(short: Short)

    public fun storeLong(long: Long)

    public fun storeInt(value: Int, bits: Int = Int.SIZE_BITS)

    public fun storeLong(value: Long, bits: Int = Long.SIZE_BITS)

    public fun storeBigInt(value: org.ton.bigint.BigInt, bits: Int)
}
