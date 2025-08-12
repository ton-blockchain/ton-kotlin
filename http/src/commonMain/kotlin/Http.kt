package org.ton.kotlin.http

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.ton.kotlin.adnl.AdnlNode
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
    fun connection(adnlNode: AdnlNode) =
        connection(rldp.connection(adnlNode))

    fun connection(adnl: AdnlPeerPair) = connection(rldp.connection(adnl))
    fun connection(rldp: RldpConnection) = HttpConnection(rldp)
}

class HttpConnection(
    val rldpConnection: RldpConnection
) : HttpService {
    private val logger = KtorSimpleLogger("org.ton.kotlin.http.HttpConnection")
    val activeRequests = Hash256Map<ByteReadChannel>()

    init {
        rldpConnection.onQuery(httpServerHandler(activeRequests))
    }

//    @OptIn(DelicateCoroutinesApi::class)
//    suspend fun request(
//        method: String,
//        url: String,
//        httpVersion: String = "HTTP/1.1",
//        headers: List<HttpHeader> = emptyList(),
//        output: ByteReadChannel,
//        responseBody: ByteChannel,
//        coroutineContext: CoroutineContext = EmptyCoroutineContext
//    ): HttpResponse = coroutineScope {
//        val id = ByteString(*kotlin.random.Random.nextBytes(32))
//        activeRequests[id] = output
//        logger.debug { "start requesting $url" }
//        val response = request(
//            id, method, url, httpVersion, headers
//        )
//        logger.trace { "got response: $response" }
////            GlobalScope.writer(coroutineContext + CoroutineName("getNextPayloadLoop"), responseBody) {
////                logger.trace { "start getNextPayloadLoop" }
////                var seqno = 0
////                while (!response.noPayload) {
////                    val part = getNextPayloadPart(id, seqno++, 1024 * 1024)
////                    logger.trace { "got getNextPayloadPart with seqno: $seqno, $part" }
////                    channel.writeByteArray(part.data.toByteArray())
////                    if (part.last) break
////                }
////                logger.trace { "end getNextPayloadLoop" }
////            }
//        logger.trace { "return response" }
//        response
//    }

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
private fun HttpConnection.httpServerHandler(
    activeRequests: Hash256Map<ByteReadChannel>
): RldpQueryHandler {
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
                    req.headers.toKtor(headers)
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
                    headers = ktorResponse.headers.toRldp(),
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

internal fun Headers.toRldp() = entries().flatMap { (name, values) ->
    values.map {
        HttpHeader(name, it)
    }
}

internal fun List<HttpHeader>.toKtor(builder: HeadersBuilder = HeadersBuilder()): HeadersBuilder {
    val headers = groupBy({ it.name }, { it.value })
    builder.appendAll(headers)
    return builder
}
