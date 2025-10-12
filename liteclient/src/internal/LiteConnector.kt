package org.ton.lite.client.internal

import io.ktor.util.collections.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.tl.TL

internal class LiteConnector(
    private val transport: LiteTransport,
    val isServer: Boolean,
) {
    val transportScope: CoroutineScope = transport
    private val sendChannel = Channel<TransportMessage>(Channel.UNLIMITED)

    private val sendJob = transportScope.launch(CoroutineName("lite-connector-send-loop")) {
        while (true) {
            transport.send(sendChannel.receiveCatching().getOrNull() ?: break)
        }
    }
    private val receiveJob = transportScope.launch(CoroutineName("lite-connector-receive-loop")) {
        while (true) {
            processMessage(transport.receiveCatching().getOrNull() ?: break)
        }
    }
    private val answerSubscriptions = ConcurrentMap<ByteString, suspend (LiteMessage.Answer) -> Unit>()

    init {
        transportScope.coroutineContext.job.invokeOnCompletion {
            answerSubscriptions.clear()
            sendChannel.close(it)
        }
    }

    fun subscribeToAnswer(
        queryId: ByteString,
        subscription: suspend (LiteMessage.Answer) -> Unit,
    ) {
        answerSubscriptions.computeIfAbsent(queryId) {
            subscription
        }
    }

    fun unsubscribeFromAnswers(
        queryId: ByteString
    ) {
        answerSubscriptions.remove(queryId)
    }

    suspend fun sendMessage(message: LiteMessage) {
        val data = TL.Boxed.encodeToByteArray(LiteMessage.serializer(), message)
        sendTransportMessage(TransportMessage(data))
    }

    suspend fun sendTransportMessage(transportMessage: TransportMessage) {
        sendChannel.send(transportMessage)
    }

    private fun decodeMessage(transportMessage: TransportMessage): LiteMessage? {
        try {
            return TL.Boxed.decodeFromByteArray<LiteMessage>(transportMessage.value)
        } catch (e: SerializationException) {
        } catch (e: IllegalArgumentException) {
        }
        return null
    }

    private suspend fun processMessage(transportMessage: TransportMessage) {
        val message = decodeMessage(transportMessage) ?: return
        processMessage(message)
    }

    private suspend fun processMessage(message: LiteMessage) {
        when (message) {
            is LiteMessage.Query -> processQuery(message)
            is LiteMessage.Answer -> processAnswer(message)
            is LiteMessage.Authentificate -> processAuthentificate(message)
            is LiteMessage.AuthentificationComplete -> processAuthentificationComplete(message)
            is LiteMessage.AuthentificationNonce -> processAuthentificationNonce(message)
            is LiteMessage.Ping -> processPing(message)
            is LiteMessage.Pong -> processPong(message)
        }
    }

    private fun processQuery(message: LiteMessage.Query) {

    }

    private suspend fun processAnswer(message: LiteMessage.Answer) {
        answerSubscriptions[message.queryId]?.invoke(message)
    }

    private fun processAuthentificate(message: LiteMessage.Authentificate) {

    }

    private fun processAuthentificationComplete(message: LiteMessage.AuthentificationComplete) {

    }

    private fun processAuthentificationNonce(message: LiteMessage.AuthentificationNonce) {

    }

    private fun processPing(message: LiteMessage.Ping) {

    }

    private fun processPong(message: LiteMessage.Pong) {

    }
}
