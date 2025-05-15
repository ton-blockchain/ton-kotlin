@file:Suppress("NOTHING_TO_INLINE")

package org.ton.proxy.adnl

import io.ktor.util.collections.*
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ton.kotlin.adnl.adnl.AdnlPacketContents
import org.ton.kotlin.adnl.adnl.message.*
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.adnl.pub.PublicKeyEd25519
import org.ton.kotlin.bitstring.BitString
import org.ton.logger.Logger
import org.ton.logger.PrintLnLogger
import org.ton.proxy.adnl.channel.AdnlChannel
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.Volatile
import kotlin.random.Random
import kotlin.time.Duration

interface AdnlPeerSession : CoroutineScope, AdnlPacketReceiver {
    val adnl: Adnl
    val localKey: PrivateKeyEd25519
    val remoteKey: PublicKeyEd25519

    suspend fun query(payload: ByteArray, timeout: Duration, maxAnswerSize: Long = Int.MAX_VALUE.toLong()): ByteArray

    suspend fun sendMessage(payload: ByteArray)

    fun receiveDatagram(payload: ByteArray, channel: AdnlChannel?)
}

abstract class AbstractAdnlPeerSession(
    final override val adnl: Adnl,
    override val localKey: PrivateKeyEd25519,
    override val remoteKey: PublicKeyEd25519,
) : AdnlPeerSession {
    private var channel: AdnlChannel? by atomic(null)
    private val logger = PrintLnLogger({ toString() }, Logger.Level.INFO)
    override val coroutineContext: CoroutineContext = adnl.coroutineContext + CoroutineName(toString())

    private val messagesQueue = Channel<AdnlMessage>()
    private val queries = ConcurrentMap<BitString, CompletableDeferred<ByteArray>>()

    @Volatile
    private var inputSeqno = 0L

    @Volatile
    private var outputSeqno = 0L

    @Volatile
    private var ackSeqno = 0L

    @Volatile
    private var recvSeqnoMask = 0L
    private val receiveLock = reentrantLock()
    private val flushMessagesJob = launch(CoroutineName("Flush messages")) {
        try {
            while (isActive) {
                flushMessage()
            }
        } finally {
            messagesQueue.close()
            queries.clear()
        }
    }

    fun createChannel(publicKey: PublicKeyEd25519, date: Instant): AdnlChannel {
        var channel = channel
        if (channel != null) {
            val currentPublicKey = channel.publicKey
            val currentDate = channel.date
            if (publicKey == currentPublicKey) {
                logger.info { "Channel already created with key: $currentPublicKey" }
                return channel
            }
            if (date < currentDate) {
                logger.info { "Channel already created with date: $currentDate" }
                return channel
            }
            adnl.unregisterChannel(channel.input.id)
        }
        channel = AdnlChannel(this, localKey, publicKey)
        adnl.registerChannel(channel.input.id, channel)
        this.channel = channel
        return channel
    }

    fun confirmChannel(publicKey: PublicKeyEd25519, date: Instant) {
        val channel = channel ?: createChannel(publicKey, date)
        channel.isReady = true
        logger.debug { "Confirmed channel with $publicKey $date" }
    }

    override fun receivePacket(packet: AdnlPacketContents) {
        val packetSeqno = packet.seqno ?: 0L
        receiveLock.withLock {
            if (packetSeqno > 0L) {
                if (receivedPacket(packetSeqno)) {
                    logger.warn { "dropping input message, old seqno: $packetSeqno (current max $inputSeqno)" }
                    return
                } else {
                    addReceivedPacket(packetSeqno)
                }
            }
            val packetConfirmSeqno = packet.confirm_seqno ?: 0L
            if (packetConfirmSeqno > 0L) {
                if (packetConfirmSeqno > outputSeqno) {
                    logger.warn { "dropping input message, bad confirm seqno: $packetConfirmSeqno (current max $outputSeqno)" }
                    return
                } else {
                    ackSeqno = packetConfirmSeqno
                }
            }
        }
        super.receivePacket(packet)
    }

    override fun receiveAdnlMessage(message: AdnlMessage) {
        logger.debug { "Received message: $message" }
        super.receiveAdnlMessage(message)
    }

    override fun receiveAdnlCreateChannel(message: AdnlMessageCreateChannel) {
        val publicKey = PublicKeyEd25519(message.key.toByteArray())
        val date = message.date()
        createChannel(publicKey, date)
    }

    override fun receiveAdnlConfirmChannel(message: AdnlMessageConfirmChannel) {
        val publicKey = PublicKeyEd25519(message.key.toByteArray())
        val date = message.date()
        return confirmChannel(publicKey, date)
    }

    override fun receiveAdnlAnswer(message: AdnlMessageAnswer) {
        val queryDeferred = queries[message.query_id]
        if (queryDeferred != null) {
            queryDeferred.complete(message.answer)
        } else {
            logger.warn { "Dropping message, unknown query id: ${message.query_id}" }
        }
    }

    override fun receiveAdnlCustom(message: AdnlMessageCustom) {
        logger.info { "Custom message: $message" }
    }

    override fun receiveNop(message: AdnlMessageNop) {
        logger.info { "Nop message: $message" }
    }

    override suspend fun query(payload: ByteArray, timeout: Duration, maxAnswerSize: Long): ByteArray {
        val queryId = BitString(Random.nextBytes(32))
        val query = AdnlMessageQuery(queryId, payload)
        val deferred = CompletableDeferred<ByteArray>()
        queries[queryId] = deferred
        try {
            return withTimeout(timeout) {
                sendMessage(query)
                deferred.await()
            }
        } catch (e: Throwable) {
            deferred.completeExceptionally(e)
            throw e
        } finally {
            queries.remove(queryId)
        }
    }

    override suspend fun sendMessage(payload: ByteArray) {
        val message = AdnlMessageCustom(payload)
        sendMessage(message)
    }

    private fun receivedPacket(seqno: Long): Boolean {
        if (seqno + 64 <= inputSeqno) {
            return true
        }
        if (seqno > inputSeqno) {
            return false
        }
        return recvSeqnoMask and (1L shl (inputSeqno - seqno).toInt()) != 0L
    }

    private fun addReceivedPacket(seqno: Long) {
        if (seqno <= inputSeqno) {
            recvSeqnoMask = recvSeqnoMask or (1L shl (inputSeqno - seqno).toInt())
        } else {
            val old = inputSeqno
            inputSeqno = seqno
            if (inputSeqno - old >= 64) {
                recvSeqnoMask = 1L
            } else {
                recvSeqnoMask = recvSeqnoMask shl (inputSeqno - old).toInt()
                recvSeqnoMask = recvSeqnoMask or 1L
            }
        }
    }

    private suspend fun sendMessage(message: AdnlMessage) {
        logger.debug { "Sending message: $message" }
        val messageSize = AdnlMessage.sizeOf(message)
        if (messageSize < MTU) {
            messagesQueue.send(message)
        } else {
            AdnlMessagePart.build(message, Adnl.MTU).forEach { messagePart ->
                messagesQueue.send(messagePart)
            }
        }
    }

    private suspend fun flushMessage() {
        val messages = ArrayList<AdnlMessage>()
        val message = messagesQueue.receive()
        val channel = channel
        val additionalMessage = if (channel == null) {
            AdnlMessageCreateChannel(
                key = BitString(localKey.publicKey().key),
                date = Clock.System.now()
            )
        } else if (!channel.isReady) {
            AdnlMessageConfirmChannel(
                key = BitString(localKey.publicKey().key),
                peerKey = BitString(channel.publicKey.key),
                date = channel.date
            )
        } else null
        if (additionalMessage != null) {
            messages.add(additionalMessage)
        }
        messages.add(message)
        sendMessages(messages, channel)
    }

    private suspend fun sendMessages(messages: List<AdnlMessage>, channel: AdnlChannel?) {
        var seqno = 0L
        var confirmSeqno = 0L
        receiveLock.withLock {
            seqno = ++outputSeqno
            confirmSeqno = inputSeqno
        }
        var packet = AdnlPacketContents(
            from = if (channel == null) localKey.publicKey() else null,
            messages = if (messages.size > 1) messages else null,
            message = if (messages.size == 1) messages.first() else null,
            address = adnl.adnlAddressList(),
            seqno = seqno,
            confirm_seqno = confirmSeqno,
            reinit_date = if (channel == null) adnl.startTime.epochSeconds.toInt() else null,
            dst_reinit_date = if (channel == null) 0 else null
        )
        if (channel != null) {
            packet = packet.signed(localKey)
        }
        sendPacket(packet, channel)
    }

    private suspend fun sendPacket(packet: AdnlPacketContents, channel: AdnlChannel?) {
        logger.debug { "Sending packet: $packet" }
        if (channel != null) {
            val datagram = packet.toByteArray()
            channel.sendDatagram(datagram)
        } else {
            val destId = remoteKey.toAdnlIdShort().id
            val encryptedPayload = remoteKey.encrypt(packet.toByteArray())
            val datagram = ByteArray(destId.size + encryptedPayload.size)
            destId.copyInto(datagram)
            encryptedPayload.copyInto(datagram, destId.size)
            adnl.sendDatagram(remoteKey.toAdnlIdShort(), datagram)
        }
    }

    override fun receiveDatagram(payload: ByteArray, channel: AdnlChannel?) {
        val decryptedPayload = if (channel == null) {
            localKey.decrypt(payload)
        } else {
            payload
        }
        val packet = AdnlPacketContents.decodeBoxed(decryptedPayload)
        receivePacket(packet)
    }

    override fun toString(): String = "ADNL[$localKey<->$remoteKey]"

    companion object {
        const val MTU = Adnl.MTU + 128
    }
}
