@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.http

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

sealed interface HttpMessage {
    val httpVersion: String
    val headers: List<HttpHeader>
}

@Serializable
@SerialName("http.response")
@TlConstructorId(0xca48a74a)
data class HttpResponse(
    override val httpVersion: String,
    val statusCode: Int,
    val reason: String,
    override val headers: List<HttpHeader>,
    val noPayload: Boolean,
) : HttpMessage

@Serializable
@SerialName("http.header")
@TlConstructorId(0x8e9be511)
data class HttpHeader(
    val name: String,
    val value: String
)

@Serializable
@SerialName("http.payloadPart")
@TlConstructorId(0x295ad764)
data class HttpPayloadPart(
    val data: ByteString,
    val trailer: List<HttpHeader>,
    val last: Boolean
)


@Serializable
@SerialName("http.proxy.capabilities")
@TlConstructorId(0x31926c11)
data class HttpProxyCapabilities(
    val capabilities: Long
)

@Serializable
sealed interface HttpFunction<T> {
    val responseSerializer: KSerializer<T>

    @Serializable
    @SerialName("http.request")
    @TlConstructorId(0x61b191e1)
    data class Request(
        @Bits256
        val id: ByteString,
        val method: String,
        val url: String,
        override val httpVersion: String,
        override val headers: List<HttpHeader>,
    ) : HttpMessage, HttpFunction<HttpResponse> {
        override val responseSerializer: KSerializer<HttpResponse>
            get() = HttpResponse.serializer()
    }

    @Serializable
    @SerialName("http.getNextPayloadPart")
    @TlConstructorId(0x90745d0c)
    data class GetNextPayloadPart(
        @Bits256
        val id: ByteString,
        val seqno: Int,
        val maxChunkSize: Int,
    ) : HttpFunction<HttpPayloadPart> {
        override val responseSerializer: KSerializer<HttpPayloadPart>
            get() = HttpPayloadPart.serializer()
    }

    @Serializable
    @SerialName("http.proxy.getCapabilities")
    @TlConstructorId(0xdb721f89)
    data class ProxyGetCapabilities(
        val capabilities: HttpProxyCapabilities
    ) : HttpFunction<HttpProxyCapabilities> {
        override val responseSerializer: KSerializer<HttpProxyCapabilities>
            get() = HttpProxyCapabilities.serializer()
    }
}

interface HttpService {
    suspend fun <T> query(query: HttpFunction<T>, serializer: KSerializer<HttpFunction<T>>): T

    suspend fun request(
        id: ByteString,
        method: String,
        url: String,
        httpVersion: String,
        headers: List<HttpHeader>
    ): HttpResponse = query(
        HttpFunction.Request(id, method, url, httpVersion, headers),
        HttpFunction.serializer(HttpResponse.serializer())
    )

    suspend fun getNextPayloadPart(id: ByteString, seqno: Int, maxChunkSize: Int): HttpPayloadPart = query(
        HttpFunction.GetNextPayloadPart(id, seqno, maxChunkSize),
        HttpFunction.serializer(HttpPayloadPart.serializer())
    )
}
