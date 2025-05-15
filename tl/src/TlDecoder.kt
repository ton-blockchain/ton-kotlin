package org.ton.kotlin.tl

import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.bytestring.ByteString
import kotlinx.io.write

public interface TlDecoder<T> {
    public fun decode(byteArray: ByteArray): T = decode(Buffer().also {
        it.write(byteArray)
    })

    public fun decode(byteString: ByteString): T = decode(Buffer().also {
        it.write(byteString)
    })

    public fun decode(source: Source): T = decode(TlReader(source))
    public fun decode(reader: TlReader): T

    public fun decodeBoxed(byteArray: ByteArray): T = decodeBoxed(Buffer().also {
        it.write(byteArray)
    })

    public fun decodeBoxed(byteString: ByteString): T = decodeBoxed(Buffer().also {
        it.write(byteString)
    })

    public fun decodeBoxed(source: Source): T = decodeBoxed(TlReader(source))
    public fun decodeBoxed(reader: TlReader): T
}
