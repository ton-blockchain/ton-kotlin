package org.ton.lite.client.internal

import io.ktor.util.collections.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.tl.TL
import kotlin.concurrent.Volatile
import kotlin.properties.Delegates
import kotlin.random.Random

internal abstract class LiteConnection {
    private var isTransportReady: Boolean = false
    private var transport: LiteTransport by Delegates.notNull()
    private val transportInitializationLock = Mutex()

    private var sendJob: Job? = null
    private var receiveJob: Job? = null
    private var sendChannel: Channel<TransportMessage>? = null
    private val answerSubscriptions =
        ConcurrentMap<ByteString, suspend (LiteMessage.Answer) -> Unit>()

    @Volatile
    private var clientCancelled = false

    protected abstract suspend fun initializeTransport(): LiteTransport

    private suspend fun initializeAndAwaitHandshakeCompletion() {
        if (!isTransportReady) {
            transportInitializationLock.withLock {
                if (isTransportReady) {
                    return@withLock
                }

                transport = initializeTransport()
                setupTransportLoops()
                isTransportReady = true
            }
        }
    }

    private fun setupTransportLoops() {
        val channel = Channel<TransportMessage>(Channel.UNLIMITED)
        sendChannel = channel

        sendJob = transport.launch(CoroutineName("lite-connection-send-loop")) {
            while (true) {
                val message = channel.receiveCatching().getOrNull() ?: break
                transport.send(message)
            }
        }

        receiveJob = transport.launch(CoroutineName("lite-connection-receive-loop")) {
            while (true) {
                val incoming = transport.receiveCatching().getOrNull() ?: break
                processTransportMessage(incoming)
            }
        }

        transport.coroutineContext.job.invokeOnCompletion {
            sendJob?.cancel()
            receiveJob?.cancel()
            clientCancelled = true
            answerSubscriptions.clear()
            channel.close(it)
            sendChannel = null
        }
    }

    suspend fun call(
        call: ByteArray,
    ): ByteArray {
        if (clientCancelled) {
            error("LiteConnection was canceled")
        }

        initializeAndAwaitHandshakeCompletion()

        val queryId = ByteString(*Random.nextBytes(32))
        val channel = Channel<LiteMessage.Answer>()
        try {
            val query = LiteMessage.Query(queryId, ByteString(*call))

            subscribeToAnswer(queryId) { message ->
                channel.send(message)
            }

            sendMessage(query)

            return channel.receiveCatching().getOrThrow().query.toByteArray()
        } catch (e: CancellationException) {
            throw e
        } finally {
            channel.close()
            unsubscribeFromAnswers(queryId)
        }
    }

    private fun subscribeToAnswer(
        queryId: ByteString,
        subscription: suspend (LiteMessage.Answer) -> Unit,
    ) {
        answerSubscriptions.computeIfAbsent(queryId) {
            subscription
        }
    }

    private fun unsubscribeFromAnswers(queryId: ByteString) {
        answerSubscriptions.remove(queryId)
    }

    private suspend fun sendMessage(message: LiteMessage) {
        val data = TL.Boxed.encodeToByteArray(LiteMessage.serializer(), message)
        sendTransportMessage(TransportMessage(data))
    }

    private suspend fun sendTransportMessage(transportMessage: TransportMessage) {
        val channel = checkNotNull(sendChannel) {
            "Transport channel is not initialized"
        }
        channel.send(transportMessage)
    }

    private suspend fun processTransportMessage(transportMessage: TransportMessage) {
        val message = decodeMessage(transportMessage) ?: return
        processMessage(message)
    }

    private fun decodeMessage(transportMessage: TransportMessage): LiteMessage? {
        try {
            return TL.Boxed.decodeFromByteArray<LiteMessage>(transportMessage.value)
        } catch (_: SerializationException) {
        } catch (_: IllegalArgumentException) {
        }
        return null
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

    private fun processQuery(message: LiteMessage.Query) {}

    private suspend fun processAnswer(message: LiteMessage.Answer) {
        answerSubscriptions[message.queryId]?.invoke(message)
    }

    private fun processAuthentificate(message: LiteMessage.Authentificate) {}

    private fun processAuthentificationComplete(message: LiteMessage.AuthentificationComplete) {}

    private fun processAuthentificationNonce(message: LiteMessage.AuthentificationNonce) {}

    private fun processPing(message: LiteMessage.Ping) {}

    private fun processPong(message: LiteMessage.Pong) {}
}
