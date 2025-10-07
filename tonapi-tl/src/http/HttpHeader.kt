package org.ton.api.http

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

@SerialName("http.header")
@Serializable
public data class HttpHeader(
    val name: String,
    val value: String
) {
    public companion object : TlCodec<HttpHeader> by HttpHeaderTlConstructor
}

public inline operator fun Iterable<HttpHeader>.get(name: String): String? =
    firstOrNull { it.name == name }?.value

public inline fun Iterable<HttpHeader>.getAll(name: String): Sequence<String> =
    asSequence().filter { it.name == name }.map { it.value }

private object HttpHeaderTlConstructor : TlConstructor<HttpHeader>(
    schema = "http.header name:string value:string = http.Header"
) {
    override fun decode(input: TlReader): HttpHeader {
        val name = input.readString()
        val value = input.readString()
        return HttpHeader(name, value)
    }

    override fun encode(output: TlWriter, value: HttpHeader) {
        output.writeString(value.name)
        output.writeString(value.value)
    }
}
