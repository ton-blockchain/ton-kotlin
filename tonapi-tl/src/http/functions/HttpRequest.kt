package org.ton.api.http.functions

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.http.HttpHeader
import org.ton.api.http.HttpResponse
import org.ton.tl.*

@SerialName("http.request")
@Serializable
public data class HttpRequest(
    @Serializable(ByteStringBase64Serializer::class)
    val id: ByteString,
    val method: String,
    val url: String,
    val http_version: String,
    val headers: Collection<HttpHeader>
) : TLFunction<HttpRequest, HttpResponse> {
    override fun tlCodec(): TlCodec<HttpRequest> = Companion

    override fun resultTlCodec(): TlCodec<HttpResponse> = HttpResponse

    public companion object : TlConstructor<HttpRequest>(
        schema = "http.request id:int256 method:string url:string http_version:string headers:(vector http.header) = http.Response"
    ) {
        override fun decode(input: TlReader): HttpRequest {
            val id = input.readByteString(32)
            val method = input.readString()
            val url = input.readString()
            val http_version = input.readString()
            val headers = input.readVector {
                HttpHeader.decode(input)
            }
            return HttpRequest(id, method, url, http_version, headers)
        }

        override fun encode(output: TlWriter, value: HttpRequest) {
            output.writeRaw(value.id)
            output.writeString(value.method)
            output.writeString(value.url)
            output.writeString(value.http_version)
            output.writeVector(value.headers) {
                write(HttpHeader, it)
            }
        }
    }
}
