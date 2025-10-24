package  org.ton.kotlin.provider.liteapi.internal

import kotlinx.coroutines.CoroutineScope

internal class TransportMessage(
    val value: ByteArray
)

/**
 * An abstraction of transport capabilities for LiteClient and LiteServer
 *
 * [CoroutineScope] is used to define connection's lifetime.
 * If canceled, no messages will be able to go to the other side,
 * so ideally, it should be canceled only after its client or server is.
 */
internal interface LiteTransport : CoroutineScope {
    suspend fun send(message: TransportMessage)

    suspend fun receive(): TransportMessage
}

internal suspend fun LiteTransport.receiveCatching(): Result<TransportMessage> {
    return runCatching { receive() }
}
