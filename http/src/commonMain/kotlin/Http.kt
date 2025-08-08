package org.ton.kotlin.http

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.ton.kotlin.adnl.AdnlAddress
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.adnl.AdnlPeerPair
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.rldp.RldpConnection
import org.ton.kotlin.rldp.RldpLocalNode
import org.ton.kotlin.rldp.RldpMessage
import org.ton.kotlin.rldp.RldpQueryHandler
import org.ton.kotlin.tl.TL


class HttpLocalNode(
    val rldp: RldpLocalNode
) {
    fun connection(adnlIdFull: AdnlIdFull, initialAddress: AdnlAddress) =
        connection(rldp.connection(adnlIdFull, initialAddress))

    fun connection(adnl: AdnlPeerPair) = connection(rldp.connection(adnl))
    fun connection(rldp: RldpConnection) = HttpConnection(rldp)
}

class HttpConnection(
    val rldpConnection: RldpConnection
) : HttpService {
    init {
        rldpConnection.onQuery(httpHandler())
    }

    suspend fun request(
        method: String,
        url: String,
        httpVersion: String = "HTTP/1.1",
        headers: List<HttpHeader> = emptyList(),
    ): Pair<HttpResponse, Flow<HttpPayloadPart>> {
        val id = ByteString(*kotlin.random.Random.nextBytes(32))
        val response = request(
            id, method, url, httpVersion, headers
        )
        if (response.noPayload) {
            return response to emptyFlow()
        }
        val payloadFlow = flow {
            var seqno = 0
            while (true) {
                val part = getNextPayloadPart(id, seqno++, 1024 * 1024)
                emit(part)
                if (part.last) break
            }
        }
        return response to payloadFlow
    }

    override suspend fun <T> query(
        query: HttpFunction<T>,
        serializer: KSerializer<HttpFunction<T>>
    ): T {
        val queryBytes = TL.Boxed.encodeToByteString(serializer, query)
        val answerBytes = rldpConnection.query(queryBytes)
        return TL.Boxed.decodeFromByteString(query.responseSerializer, answerBytes)
    }
}

@OptIn(InternalSerializationApi::class, InternalAPI::class)
private fun HttpConnection.httpHandler(): RldpQueryHandler {
    val activeRequests = Hash256Map<ByteReadChannel>()
    return handler@{ transferId: ByteString, query: RldpMessage.Query ->
        val req = try {
            TL.Boxed.decodeFromByteString(HttpFunction::class.serializer(), query.data)
        } catch (e: Throwable) {
            return@handler
        }
        when (req) {
            is HttpFunction.Request -> {
                val ktorClient = HttpClient(CIO)
                val ktorResponse = ktorClient.request {
                    url(req.url)
                    method = HttpMethod.parse(req.method)
                    headers {
                        req.headers.forEach { header ->
                            appendAll(valuesOf(header.name, header.value))
                        }
                    }
                    if (method.supportsRequestBody) {
                        setBody(
                            ChannelWriterContent(
                                body = {
                                    var seqno = 0
                                    while (true) {
                                        val part = getNextPayloadPart(req.id, seqno++, 1024 * 1024)
                                        writeFully(part.data.toByteArray())
                                        flush()
                                        if (part.last) {
                                            break
                                        }
                                    }
                                    flushAndClose()
                                },
                                null
                            )
                        )
                    }
                }
                activeRequests[req.id] = ktorResponse.rawContent
                val response = HttpResponse(
                    statusCode = ktorResponse.status.value,
                    reason = ktorResponse.status.description,
                    httpVersion = ktorResponse.version.toString(),
                    headers = ktorResponse.headers.entries().map { (name, value) ->
                        HttpHeader(name, value.joinToString(", "))
                    },
                    noPayload = ktorResponse.contentLength() == 0L
                )
                val answer = RldpMessage.Answer(
                    queryId = query.queryId,
                    data = TL.Boxed.encodeToByteString(HttpResponse.serializer(), response)
                )
                rldpConnection.sendAnswer(transferId, answer)
            }

            is HttpFunction.GetNextPayloadPart -> {
                val stream = activeRequests[req.id] ?: return@handler
                var isLast = false
                try {
                    val buffer = ByteArray(req.maxChunkSize)
                    val readBytes = stream.readAvailable(buffer)
                    isLast = stream.isClosedForRead || readBytes == -1
                    val response = HttpPayloadPart(
                        data = ByteString(buffer, 0, readBytes),
                        trailer = emptyList(),
                        last = stream.isClosedForRead || readBytes == -1
                    )
                    val answer = RldpMessage.Answer(
                        queryId = query.queryId,
                        data = TL.Boxed.encodeToByteString(HttpPayloadPart.serializer(), response)
                    )
                    rldpConnection.sendAnswer(transferId, answer)
                } catch (e: Throwable) {
                    isLast = true
                    throw e
                } finally {
                    if (isLast) {
                        activeRequests.remove(transferId)
                    }
                }
            }

            is HttpFunction.ProxyGetCapabilities -> {
            }
        }
    }
}
