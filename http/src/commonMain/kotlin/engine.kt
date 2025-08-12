package org.ton.kotlin.http

import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.date.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.io.IOException
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.AdnlAddressResolver
import org.ton.kotlin.adnl.AdnlIdShort
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

private val LOGGER by lazy { KtorSimpleLogger("RldpClientHttpEngine") }

class RldpClientHttpEngine(
    private val httpLocalNode: HttpLocalNode,
    private val adnlAddressResolver: AdnlAddressResolver,
    override val config: HttpClientEngineConfig = HttpClientEngineConfig(),
) : HttpClientEngineBase("rldp") {

    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val callContext = callContext()
        val host = data.url.host
        if (host.endsWith(".adnl")) {
            val adnlId = host.dropLast(5).decodeAdnlId()
            val adnlNode =
                adnlAddressResolver.resolveAddress(adnlId) ?: throw IOException("Can't resolve address for $adnlId")
            val connection = httpLocalNode.connection(adnlNode)
            while (coroutineContext.isActive) {
                try {
                    val requestTime = GMTDate()
                    return sendRequest(requestTime, connection, adnlId, data, callContext)
                } catch (_: ClosedSendChannelException) {
                    continue
                }
            }
        }
        throw IllegalArgumentException("Can't request $host, only .adnl addresses are supported")
    }

    @OptIn(InternalAPI::class)
    private suspend fun sendRequest(
        requestTime: GMTDate,
        connection: HttpConnection,
        adnlIdShort: AdnlIdShort,
        request: HttpRequestData,
        callContext: CoroutineContext,
        closeChannel: Boolean = true
    ): HttpResponseData = withContext(callContext) {
        LOGGER.debug { "sending request to $adnlIdShort" }

        val callId = ByteString(Random.nextBytes(32))
        val output = ByteChannel()
        connection.activeRequests[callId] = output

        writeRequest(request, output, callContext)
        val rawResponse = try {
            connection.request(
                id = callId,
                method = request.method.value,
                url = request.url.toString(),
                headers = request.headers.toRldp(),
                httpVersion = HttpProtocolVersion.HTTP_1_1.toString(),
            )
        } catch (cause: Throwable) {
            if (closeChannel) {
                output.flushAndClose()
            }
            throw cause
        }
        LOGGER.trace { "received response: $rawResponse\n\n" }
        val status = HttpStatusCode(rawResponse.statusCode, rawResponse.reason)
        val headers = rawResponse.headers.toKtor().build()
        val version = HttpProtocolVersion.parse(rawResponse.httpVersion)
        val body = when {
            request.method == HttpMethod.Head ||
                    status in listOf(HttpStatusCode.NotModified, HttpStatusCode.NoContent) ||
                    status.isInformational() -> ByteReadChannel.Empty

            else -> {
                val scope = CoroutineScope(callContext + CoroutineName("body-receiver"))
                val receiver = scope.writer(autoFlush = true) {
                    receiveResponseBody(callId, connection, channel)
                }
                receiver.channel
            }
        }

        val responseBody: Any = request.attributes.getOrNull(ResponseAdapterAttributeKey)
            ?.adapt(request, status, headers, body, request.body, callContext)
            ?: body

        return@withContext HttpResponseData(status, requestTime, headers, version, responseBody, callContext)
    }
}

private const val MAX_CHUNK_SIZE_LENGTH = 1024 * 1024 // 1 MB

private suspend fun receiveResponseBody(
    callId: ByteString,
    connection: HttpConnection,
    output: ByteWriteChannel
) {
    LOGGER.trace { "start receiving response body" }

    var seqno = 0
    var totalBytesReceived = 0L

    try {
        while (true) {
            val payloadPart = connection.getNextPayloadPart(callId, seqno++, MAX_CHUNK_SIZE_LENGTH)
//            LOGGER.trace { "payload part: $payloadPart" }
            if (payloadPart.data.size > 0) {
                output.writeByteArray(payloadPart.data.toByteArray())
                output.flush()
                totalBytesReceived++
            }

            if (payloadPart.last) break
        }
    } catch (t: Throwable) {
        output.close(t)
        throw t
    } finally {
        output.flushAndClose()
    }
}

private suspend fun writeRequest(
    request: HttpRequestData,
    output: ByteWriteChannel,
    callContext: CoroutineContext,
    closeChannel: Boolean = true
) = withContext(callContext) {
    val body = request.body.getUnwrapped()
    val scope = CoroutineScope(callContext + CoroutineName("request-body-writer"))
    scope.launch {
        try {
            processOutgoingContent(request, body, output)
        } catch (cause: Throwable) {
            output.close(cause)
            throw cause
        } finally {
            output.flush()
            output.closedCause?.unwrapCancellationException()?.takeIf { it !is CancellationException }?.let {
                throw it
            }
            if (closeChannel) {
                output.flushAndClose()
            }
        }
    }
}

private suspend fun processOutgoingContent(request: HttpRequestData, body: OutgoingContent, channel: ByteWriteChannel) {
    when (body) {
        is OutgoingContent.ByteArrayContent -> channel.writeFully(body.bytes())
        is OutgoingContent.ReadChannelContent -> body.readFrom().copyAndClose(channel)
        is OutgoingContent.WriteChannelContent -> body.writeTo(channel)
        is OutgoingContent.ContentWrapper -> processOutgoingContent(request, body.delegate(), channel)
        is OutgoingContent.ProtocolUpgrade -> error("unreachable code")
        is OutgoingContent.NoContent -> {}
    }
}

private fun OutgoingContent.getUnwrapped(): OutgoingContent = when (this) {
    is OutgoingContent.ContentWrapper -> delegate().getUnwrapped()
    else -> this
}

private fun HttpStatusCode.isInformational(): Boolean = (value / 100) == 1
