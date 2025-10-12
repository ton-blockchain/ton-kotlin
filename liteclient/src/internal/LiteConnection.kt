package org.ton.lite.client.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.bytestring.ByteString
import kotlin.concurrent.Volatile
import kotlin.properties.Delegates
import kotlin.random.Random

internal abstract class LiteConnection() {
    private var isTransportReady: Boolean = false
    private var transport: LiteTransport by Delegates.notNull()
    private val transportInitializationLock = Mutex()

    @Volatile
    private var clientCancelled = false

    private val connector by lazy {
        checkTransportReadiness()
        LiteConnector(transport, isServer = false)
    }

    protected abstract suspend fun initializeTransport(): LiteTransport

    private suspend fun initializeAndAwaitHandshakeCompletion() {
        if (!isTransportReady) {
            transportInitializationLock.withLock {
                if (isTransportReady) {
                    return@withLock
                }

                transport = initializeTransport()
                isTransportReady = true
            }
        }
    }

    private fun checkTransportReadiness() {
        if (!isTransportReady) {
            error(
                "Internal error, please contact developers for the support. " +
                        "Transport is not initialized, first scope access must come from an RPC request."
            )
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

            connector.subscribeToAnswer(queryId) { message ->
                channel.send(message)
            }

            connector.sendMessage(query)

            return channel.receiveCatching().getOrThrow().query.toByteArray()
        } catch (e: CancellationException) {
            throw e
        } finally {
            channel.close()
            connector.unsubscribeFromAnswers(queryId)
        }
    }
}
