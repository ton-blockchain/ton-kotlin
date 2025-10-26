package org.ton.sdk.bitstring

import kotlinx.io.Sink
import kotlinx.io.bytestring.ByteString
import org.ton.sdk.bigint.BigInt

public interface BitSource {
    public fun readBit(): Boolean

    public fun readByte(): Byte

    public fun readInt(): Int

    public fun readShort(): Short

    public fun readLong(): Long

    public fun skip(bitCount: Int)

    public fun readAtMostTo(sink: ByteArray, startIndex: Int = 0, endIndex: Int = sink.size): Int

    public fun readAtMostTo(sink: BitSink, bitCount: Int): Int

    public fun transferTo(sink: BitSink): Int

    public fun readInt(bitCount: Int): Int

    public fun readLong(bitCount: Int): Long

    public fun readBigInt(bitCount: Int): BigInt

    public fun readBitString(bitCount: Int): BitString

    public fun readByteArray(byteCount: Int): ByteArray

    public fun readByteString(byteCount: Int): ByteString

    public fun readTo(sink: BitSink, bitCount: Int)

    public fun readTo(sink: Sink, byteCound: Int)
}
