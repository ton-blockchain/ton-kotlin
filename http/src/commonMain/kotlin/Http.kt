package org.ton.kotlin.http

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.ton.kotlin.adnl.AdnlNode
import org.ton.kotlin.adnl.AdnlQuery
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.rldp.RldpConnection
import org.ton.kotlin.rldp.RldpLocalNode
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


class HttpLocalNode(
    val rldp: RldpLocalNode,
) : CoroutineScope by rldp {
    override val coroutineContext: CoroutineContext = rldp.coroutineContext + CoroutineName("http-local-node")

    init {
        launch {
            rldp.adnlLocalNode.queries.collect {
                connection(it.peerPair.remoteNode).processQuery(it)
            }
        }
    }

    suspend fun connection(adnlNode: AdnlNode) =
        connection(rldp.connection(adnlNode))

    fun connection(rldp: RldpConnection) = HttpConnection(rldp)
}

class HttpConnection(
    val rldpConnection: RldpConnection
) : HttpService {
    private val logger = KtorSimpleLogger("org.ton.kotlin.http.HttpConnection")
    val activeRequests = Hash256Map<ByteString, ByteReadChannel>({ it })

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun request(
        method: String,
        url: String,
        httpVersion: String = "HTTP/1.1",
        headers: List<HttpHeader> = emptyList(),
        output: ByteReadChannel,
        responseBody: ByteChannel,
        coroutineContext: CoroutineContext = EmptyCoroutineContext
    ): HttpResponse = coroutineScope {
        val id = ByteString(*kotlin.random.Random.nextBytes(32))
        activeRequests[id] = output
        logger.debug { "start requesting $url" }
        val response = request(
            id, method, url, httpVersion, headers
        )
        logger.trace { "got response: $response" }
        GlobalScope.writer(coroutineContext + CoroutineName("getNextPayloadLoop"), responseBody) {
            logger.trace { "start getNextPayloadLoop" }
            var seqno = 0
            while (!response.noPayload) {
                val part = getNextPayloadPart(id, seqno++, 1024 * 1024)
                logger.trace { "got getNextPayloadPart with seqno: $seqno, $part" }
                channel.writeByteArray(part.data.toByteArray())
                if (part.last) break
            }
            logger.trace { "end getNextPayloadLoop" }
        }
        logger.trace { "return response" }
        response
    }

    override suspend fun <T> query(
        query: HttpFunction<T>,
        serializer: KSerializer<HttpFunction<T>>
    ): T {
        val queryBytes = TL.Boxed.encodeToByteString(serializer, query)
        val answerBytes = rldpConnection.query(queryBytes)
        return TL.Boxed.decodeFromByteString(query.responseSerializer, answerBytes)
    }

    @OptIn(InternalAPI::class, InternalSerializationApi::class)
    internal suspend fun processQuery(
        query: AdnlQuery
    ) {
        val req = try {
            TL.Boxed.decodeFromByteString(HttpFunction::class.serializer(), query.input)
        } catch (_: Throwable) {
            return
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
                                        val part = getNextPayloadPart(req.id, seqno++, 1024 * 1024 * 2)
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
                query.respond(TL.Boxed.encodeToByteString(response))
            }

            is HttpFunction.GetNextPayloadPart -> {
                val stream = activeRequests[req.id] ?: return
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
                    query.respond(TL.Boxed.encodeToByteString(HttpPayloadPart.serializer(), response))
                } catch (e: Throwable) {
                    isLast = true
                    throw e
                } finally {
                    if (isLast) {
                        activeRequests.remove(req.id)
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
