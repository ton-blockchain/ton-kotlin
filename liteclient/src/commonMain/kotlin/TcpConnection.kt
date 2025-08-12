package org.ton.kotlin.liteclient

import io.github.andreypfau.kotlinx.crypto.AES
import io.github.andreypfau.kotlinx.crypto.CTRBlockCipher
import io.github.andreypfau.kotlinx.crypto.Sha256
import io.github.andreypfau.kotlinx.crypto.sha256
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import org.ton.kotlin.crypto.PublicKeyEd25519
import kotlin.random.Random

class TcpConnection(
    val connection: Connection,
    val remoteKey: PublicKeyEd25519,
    coroutineScope: CoroutineScope
) : CoroutineScope by coroutineScope {
    private val chanelCipher = async(start = CoroutineStart.LAZY) {
        initializeChannel()
    }

    suspend fun send(byteArray: ByteArray) {
        chanelCipher.await().send(byteArray)
    }

    suspend fun receive() = chanelCipher.await().receive()

    private suspend fun initializeChannel(): ChannelCipher {
        return with(this@TcpConnection.connection.output) {
            val nonce = Random.nextBytes(160)
            val encryptedNonce = remoteKey.createEncryptor().encryptToByteArray(nonce)

            writeByteArray(remoteKey.computeShortId().toByteArray())
            writeByteArray(encryptedNonce)
            flush()

            ChannelCipher(this@TcpConnection.connection.input, this@TcpConnection.connection.output, nonce).apply {
                require(receive().isEmpty())
            }
        }
    }

    private class ChannelCipher(
        private val input: ByteReadChannel,
        private val output: ByteWriteChannel,
        private val inputCipher: CTRBlockCipher,
        private val outputCipher: CTRBlockCipher
    ) {
        constructor(
            input: ByteReadChannel,
            output: ByteWriteChannel,
            s1: ByteArray, s2: ByteArray, v1: ByteArray, v2: ByteArray
        ) : this(input, output, CTRBlockCipher(AES(s1), v1), CTRBlockCipher(AES(s2), v2))

        constructor(
            input: ByteReadChannel,
            output: ByteWriteChannel,
            nonce: ByteArray
        ) : this(
            input,
            output,
            s1 = nonce.copyOfRange(0, 32),
            s2 = nonce.copyOfRange(32, 64),
            v1 = nonce.copyOfRange(64, 80),
            v2 = nonce.copyOfRange(80, 96),
        )

        suspend fun send(packet: ByteArray) {
            val dataSize = 32 + packet.size + 32
            val decrypted = ByteArray(4 + dataSize)
            decrypted.setIntLeAt(0, dataSize)

            Random.nextBytes(decrypted, 4, 4 + 32)
            packet.copyInto(decrypted, 4 + 32)

            with(Sha256()) {
                update(decrypted, 4, 4 + 32 + packet.size)
                digest(decrypted, 4 + 32 + packet.size)
            }

            val encrypted = ByteArray(decrypted.size)
            outputCipher.processBytes(decrypted, encrypted)
            output.writeByteArray(encrypted)
            output.flush()
        }

        suspend fun receive(): ByteArray {
            if (input.isClosedForRead) {
                throw IllegalStateException("Input channel is closed")
            }
            val len = with(inputCipher) {
                val encrypted = input.readByteArray(Int.SIZE_BYTES)
                val decrypted = ByteArray(encrypted.size)
                processBytes(encrypted, decrypted)

                decrypted.getIntLeAt(0)
            }
            val encrypted = input.readByteArray(len)
            val decrypted = ByteArray(len)
            inputCipher.processBytes(encrypted, decrypted)
            val actualHash = sha256(decrypted.copyOfRange(0, len - 32))
            val expectedHash = decrypted.copyOfRange(len - 32, len)
            if (!actualHash.contentEquals(expectedHash)) {
                throw IllegalStateException(
                    "Hash mismatch: expected ${expectedHash.toHexString()} but got ${actualHash.toHexString()}"
                )
            }
            return decrypted.copyOfRange(32, len - 32)
        }

        private fun ByteArray.getIntLeAt(index: Int): Int {
            return (this[index].toInt() and 0xFF) or
                    ((this[index + 1].toInt() and 0xFF) shl 8) or
                    ((this[index + 2].toInt() and 0xFF) shl 16) or
                    ((this[index + 3].toInt() and 0xFF) shl 24)
        }

        private fun ByteArray.setIntLeAt(index: Int, value: Int) {
            this[index] = (value and 0xFF).toByte()
            this[index + 1] = ((value ushr 8) and 0xFF).toByte()
            this[index + 2] = ((value ushr 16) and 0xFF).toByte()
            this[index + 3] = ((value ushr 24) and 0xFF).toByte()
        }
    }
}
