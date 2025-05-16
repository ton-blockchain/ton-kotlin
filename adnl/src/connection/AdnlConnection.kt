package org.ton.kotlin.adnl.connection

import io.github.andreypfau.kotlinx.crypto.AES
import io.github.andreypfau.kotlinx.crypto.CTRBlockCipher
import io.github.andreypfau.kotlinx.crypto.Sha256
import io.github.andreypfau.kotlinx.crypto.sha256
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.io.readByteArray
import kotlinx.io.readIntLe
import kotlinx.io.writeIntLe
import org.ton.kotlin.adnl.network.TcpClient
import org.ton.kotlin.api.liteserver.LiteServerDesc
import org.ton.kotlin.crypto.SecureRandom
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.seconds

public class AdnlConnection(
    public val liteServerDesc: LiteServerDesc,
    private val connectionFactory: AdnlConnectionFactory,
    override val coroutineContext: CoroutineContext,
    private val onDone: () -> Unit
) : CoroutineScope, Closeable {
    private val lastActivity = atomic(Clock.System.now())
    private val deliveryPoint: Channel<RequestTask> = Channel()
    private val timeout = launch(coroutineContext + CoroutineName("Connection timeout")) {
        try {
            while (true) {
                val remaining = (lastActivity.value + MAX_IDLE_TIME) - Clock.System.now()
                if (remaining <= ZERO) {
                    break
                }
                delay(remaining)
            }
        } catch (_: Throwable) {
        } finally {
            onDone()
        }
    }

    public suspend fun execute(
        request: AdnlRequestData,
        callContext: CoroutineContext
    ): AdnlResponseData {
        lastActivity.value = Clock.System.now()
        return makeDedicatedRequest(request, callContext)
    }

    private suspend fun makeDedicatedRequest(
        request: AdnlRequestData,
        callContext: CoroutineContext
    ): AdnlResponseData {
        val connection = connect()
        try {
            val nonce = SecureRandom.nextBytes(160)

            callContext[Job]!!.invokeOnCompletion { cause ->
                try {
                    connection.close(cause)
                } catch (_: Throwable) {
                }
            }

            connection.output.writeFully(liteServerDesc.id.toAdnlIdShort().id.toByteArray())
            connection.output.writeFully(liteServerDesc.id.encrypt(nonce))
            connection.output.flush()

            val cipher = ChannelCipher(nonce)
            val now = Clock.System.now()
            readResponse(now, connection.input, cipher.input, callContext)

            writeRequest(request, callContext, connection.output, cipher.output, false)
            return readResponse(now, connection.input, cipher.input, callContext)
        } catch (cause: Throwable) {
            throw cause
        } finally {
            connection.close()
        }
    }

    private suspend fun connect(): TcpClient {
        try {
            var timeoutFails = 0
            repeat(CONNECTION_ATTEMPTS) {
                val tcpClient = withTimeoutOrNull(
                    CONNECT_TIMEOUT
                ) {
                    connectionFactory.connect(liteServerDesc)
                }
                if (tcpClient == null) {
                    timeoutFails++
                    return@repeat
                }
                return tcpClient
            }
        } catch (cause: Throwable) {
            throw cause
        }
        throw IOException("Connection timeout")
    }

    override fun close() {

    }

    internal suspend fun writeRequest(
        request: AdnlRequestData,
        callContext: CoroutineContext,
        output: ByteWriteChannel,
        cipher: CTRBlockCipher,
        closeChannel: Boolean = true
    ) = withContext(callContext) {
        val scope = CoroutineScope(callContext + CoroutineName("Request body writer"))
        scope.launch {
            try {
                writeRaw(output, cipher, ByteReadPacket(request.body))
            } catch (cause: Throwable) {
                output.close(cause)
                throw cause
            } finally {
                output.flush()
                if (closeChannel) {
                    output.close()
                }
            }
        }
    }

    private suspend fun readResponse(
        requestTime: Instant,
        input: ByteReadChannel,
        cipher: CTRBlockCipher,
        callContext: CoroutineContext
    ) = withContext(callContext) {
        val packet = readRaw(input, cipher)
        return@withContext AdnlResponseData(
            requestTime,
            packet,
            callContext
        )
    }

    private suspend fun readRaw(
        input: ByteReadChannel,
        cipher: CTRBlockCipher
    ): ByteReadPacket {
        val encryptedLength = input.readPacket(4).readByteArray()
        val plainLength = ByteArray(4)
        cipher.processBytes(encryptedLength, plainLength)

        val length = ByteReadPacket(plainLength).readIntLe()
        check(length in 32..(1 shl 24)) { "Invalid length" }
        val encryptedData = input.readPacket(length).readByteArray()
        val plainData = ByteArray(length)
        cipher.processBytes(encryptedData, plainData)

        val data = ByteReadPacket(plainData)
        val payload = data.readByteArray((data.remaining - 32).toInt())
        val hash = data.readByteArray(32)

        require(sha256(payload).contentEquals(hash)) {
            "sha256 mismatch"
        }

        val packet = ByteReadPacket(payload)
        packet.discard(32) // nonce
        return packet
    }

    private suspend fun writeRaw(
        output: ByteWriteChannel,
        cipher: CTRBlockCipher,
        packet: ByteReadPacket
    ) {
        val dataSize = (packet.remaining + 32 + 32).toInt()
        require(dataSize in 32..(1 shl 24)) { "Invalid packet size: $dataSize" }
        val nonce = SecureRandom.nextBytes(32)
        val payload = packet.readByteArray()

        val hash = Sha256().apply {
            update(nonce)
            update(payload)
        }.digest()

        val data = buildPacket {
            writeIntLe(dataSize)
            writeFully(nonce)
            writeFully(payload)
            writeFully(hash)
        }

        val encryptedData = ByteArray(data.remaining.toInt())
        cipher.processBytes(data.readByteArray(), encryptedData)
        output.writeFully(encryptedData)
    }

    private class ChannelCipher(
        val input: CTRBlockCipher,
        val output: CTRBlockCipher
    ) {
        constructor(
            s1: ByteArray, s2: ByteArray, v1: ByteArray, v2: ByteArray
        ) : this(CTRBlockCipher(AES(s1), v1), CTRBlockCipher(AES(s2), v2))

        constructor(
            nonce: ByteArray
        ) : this(
            s1 = nonce.copyOfRange(0, 32),
            s2 = nonce.copyOfRange(32, 64),
            v1 = nonce.copyOfRange(64, 80),
            v2 = nonce.copyOfRange(80, 96),
        )
    }

    public companion object {
        public val MAX_IDLE_TIME: Duration = 10.seconds
        public val CONNECT_TIMEOUT: Duration = 5.seconds
        public const val CONNECTION_ATTEMPTS: Int = 1
    }
}
