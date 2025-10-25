package org.ton.sdk.bitstring

import kotlinx.io.bytestring.ByteString
import org.ton.bigint.BigInt

public interface BitSink {
    public fun transferFrom(source: BitSource): Int

    public fun write(source: BitSource, bitCount: Int)

    public fun write(source: BitString, startIndex: Int = 0, endIndex: Int = source.size)

    public fun write(source: ByteArray, startIndex: Int = 0, endIndex: Int = source.size)

    public fun write(source: ByteString, startIndex: Int = 0, endIndex: Int = source.size)

    public fun writeBit(bit: Boolean)

    public fun writeByte(byte: Byte)

    public fun writeInt(int: Int)

    public fun writeShort(short: Short)

    public fun writeLong(long: Long)

    public fun writeInt(value: Int, bits: Int)

    public fun writeLong(value: Long, bits: Int)

    public fun writeBigInt(value: BigInt, bits: Int)
}
