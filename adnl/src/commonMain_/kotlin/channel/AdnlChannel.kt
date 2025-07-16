package org.ton.kotlin.adnl.channel

import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.adnl.AdnlPacket
import org.ton.kotlin.adnl.transport.AdnlReadWriteChannel
import org.ton.kotlin.adnl.transport.AdnlTransport
import org.ton.kotlin.crypto.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class AdnlChannel private constructor(
    override val coroutineContext: CoroutineContext,
    val transport: AdnlTransport,
    val localKey: PrivateKeyEd25519,
    val remoteKey: PublicKeyEd25519,
    val inputId: AdnlIdShort,
    val outputId: AdnlIdShort,
    val encryptor: Encryptor,
    val decryptor: Decryptor,
    val date: Instant
) : AdnlReadWriteChannel, CoroutineScope {

    private val receiver = Channel<AdnlPacket>()
    private val sender = Channel<AdnlPacket>()

    init {
        launch {
            while (true) {
                select {
                    sender.onReceive {
                        val raw = AdnlPacket.serialize(it)
                        val encrypted = encryptor.encrypt(raw)
                        val buffer = Buffer()
                        buffer.writeFully(encrypted)
                        transport.send(buffer)
                    }
                    transport.incoming.onReceive {
                        val encrypted = it.readByteArray()
                        val decrypted = decryptor.decrypt(encrypted)
                        val packet = AdnlPacket.deserialize(decrypted)
                        receiver.send(packet)
                    }
                }
            }
        }
    }

    override val incoming: ReceiveChannel<AdnlPacket>
        get() = receiver
    override val outgoing: SendChannel<AdnlPacket>
        get() = sender

    companion object {
        fun create(
            coroutineContext: CoroutineContext,
            transport: AdnlTransport,
            localKey: PrivateKeyEd25519,
            remoteKey: PublicKeyEd25519,
            localId: AdnlIdShort,
            remoteId: AdnlIdShort,
            now: Instant = Clock.System.now()
        ): AdnlChannel {
            val sharedSecret = localKey.computeSharedSecret(remoteKey)
            val decryptSecret: PrivateKeyAes
            val encryptSecret: PublicKeyAes
            val compared = localId.compareTo(remoteId)
            when {
                compared < 0 -> {
                    decryptSecret = PrivateKeyAes(sharedSecret)
                    encryptSecret = PublicKeyAes(sharedSecret.reversedArray())
                }

                compared > 0 -> {
                    decryptSecret = PrivateKeyAes(sharedSecret.reversedArray())
                    encryptSecret = PublicKeyAes(sharedSecret)
                }

                else -> {
                    decryptSecret = PrivateKeyAes(sharedSecret)
                    encryptSecret = PublicKeyAes(sharedSecret)
                }
            }
            val inputId = AdnlIdShort(decryptSecret.computeShortId())
            val outputId = AdnlIdShort(encryptSecret.computeShortId())
            val encryptor = encryptSecret.createEncryptor()
            val decryptor = decryptSecret.createDecryptor()
            return AdnlChannel(
                coroutineContext,
                transport,
                localKey,
                remoteKey,
                inputId,
                outputId,
                encryptor,
                decryptor,
                now
            )
        }
    }
}
