@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.tl.constructors

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.tl.TlConstructor
import org.ton.kotlin.tl.TlReader
import org.ton.kotlin.tl.TlWriter

public object BytesTlConstructor : TlConstructor<ByteArray>(
    schema = "bytes data:string = Bytes"
) {
    public fun sizeOf(value: ByteString): Int = sizeOf(value)

    public fun sizeOf(value: ByteArray): Int {
        var size = value.size
        size += if (size < 254) {
            1
        } else if (size < (1 shl 24)) {
            4
        } else {
            8
        }
        size += size % 4
        return size
    }

    override fun decode(reader: TlReader): ByteArray {
        return reader.readBytes()
    }

    override fun encode(writer: TlWriter, value: ByteArray) {
        return writer.writeBytes(value)
    }
}
