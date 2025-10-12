package org.ton.lite.client.internal

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import org.ton.api.liteserver.LiteServerDesc
import org.ton.kotlin.crypto.AesCtr
import org.ton.kotlin.crypto.Encryptor
import org.ton.kotlin.crypto.SecureRandom
import org.ton.kotlin.crypto.Sha256
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

internal class LiteTcpTransport(
    val connection: Connection,
    val remoteId: ByteString,
    val remoteEncryptor: Encryptor,
    coroutineContext: CoroutineContext? = null
) : LiteTransport {
    override val coroutineContext: CoroutineContext =
        SupervisorJob(coroutineContext?.job) + CoroutineName("lite-tcp-transport")

    private var outputCipher: AesCtr by Delegates.notNull()
    private var inputCipher: AesCtr by Delegates.notNull()

    suspend fun handshake() {
        val nonce = SecureRandom.nextBytes(160)
        val handshakePacket = Buffer().apply {
            write(remoteId)
            writeFully(remoteEncryptor.encryptToByteArray(nonce))
        }

        connection.output.writePacket(handshakePacket)
        connection.output.flush()

        inputCipher = AesCtr(nonce.copyOfRange(0, 32), nonce.copyOfRange(64, 80))
        outputCipher = AesCtr(nonce.copyOfRange(32, 64), nonce.copyOfRange(80, 96))

        check(receive().value.isEmpty())
    }

    override suspend fun send(message: TransportMessage) {
        val dataSize = (message.value.size + 32 + 32)
        require(dataSize in 32..(1 shl 24)) { "Invalid packet size: $dataSize" }
        val nonce = Random.nextBytes(32)
        val payload = message.value

        val hash = Sha256().use { sha256 ->
            sha256.update(nonce)
            sha256.update(payload)
            sha256.digest()
        }

        val packet = with(Buffer()) {
            writeIntLe(dataSize)
            writeFully(nonce)
            writeFully(payload)
            writeFully(hash)
            readByteArray()
        }

        val encryptedPacket = ByteArray(packet.size)
        outputCipher.processBytes(packet, encryptedPacket)
        connection.output.writeFully(encryptedPacket)
        connection.output.flush()
    }

    override suspend fun receive(): TransportMessage {
        val encryptedLength = connection.input.readByteArray(4)
        val plainLength = ByteArray(4)
        inputCipher.processBytes(encryptedLength, plainLength)

        val length = with(Buffer()) {
            write(plainLength)
            readIntLe()
        }
        check(length in 32..(1 shl 24)) { "Invalid length" }
        val encryptedData = connection.input.readByteArray(length)
        val plainData = ByteArray(length)
        inputCipher.processBytes(encryptedData, plainData)

        val payload = plainData.copyOfRange(32, plainData.size - 32)
        val hash = plainData.copyOfRange(plainData.size - 32, plainData.size)

        val actualHash = Sha256().use { sha256 ->
            sha256.update(plainData, 0, plainData.size - 32)
            sha256.digest()
        }
        require(actualHash.contentEquals(hash)) {
            "sha256 mismatch"
        }

        return TransportMessage(payload)
    }
}

internal class LiteTcpConnection(
    val liteServerDesc: LiteServerDesc
) : LiteConnection(), CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + CoroutineName("lite-tcp-connection")

    override suspend fun initializeTransport(): LiteTransport {
        val remoteEncryptor = liteServerDesc.id as Encryptor
        val remoteId = liteServerDesc.id.computeShortId()
        val address = InetSocketAddress(ipv4(liteServerDesc.ip), liteServerDesc.port)
        val socket = withTimeout(5.seconds) {
            aSocket(SelectorManager()).tcp().connect(address)
        }
        val connection = socket.connection()
        val transport = LiteTcpTransport(
            connection,
            remoteId,
            remoteEncryptor,
        )
        transport.handshake()
        return transport
    }
}
