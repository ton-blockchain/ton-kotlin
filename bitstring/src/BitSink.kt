package org.ton.sdk.bitstring

import kotlinx.io.bytestring.ByteString
import org.ton.bigint.BigInt
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringApi
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

public interface BitSink {
    @UnsafeBitStringApi
    public val buffer: ByteArray

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

    public fun writeInt(value: Int, bitCount: Int)

    public fun writeUInt(value: Int, bitCount: Int)

    public fun writeLong(value: Long, bitCount: Int)

    public fun writeULong(value: Long, bitCount: Int)

    public fun writeUBigInt(value: BigInt, bitCount: Int)
}

@OptIn(UnsafeBitStringApi::class)
public inline fun BitSink.writeToInternalBuffer(lambda: (ByteArray) -> Unit) {
    contract {
        callsInPlace(lambda, EXACTLY_ONCE)
    }
    lambda(this.buffer)
}

public inline fun BitSink.writeUInt(value: UInt, bitCount: Int): Unit = writeUInt(value.toInt(), bitCount)
public inline fun BitSink.writeULong(value: ULong, bitCount: Int): Unit = writeULong(value.toLong(), bitCount)
