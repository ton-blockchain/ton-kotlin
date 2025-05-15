package org.ton.proxy.adnl

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.collections.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Clock
import org.ton.kotlin.adnl.ipv4
import org.ton.kotlin.adnl.packet.AdnlHandshakePacket
import org.ton.kotlin.adnl.adnl.AdnlAddressList
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.adnl.AdnlPacketContents
import org.ton.kotlin.adnl.adnl.message.AdnlMessage
import org.ton.kotlin.adnl.adnl.message.AdnlMessageAnswer
import org.ton.kotlin.adnl.adnl.message.AdnlMessageQuery
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.adnl.pub.PublicKey
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.tl.TLFunction
import org.ton.kotlin.tl.TlObject
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmStatic
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

open class AdnlPeer(
    val address: AdnlAddressUdp,
    val key: PublicKey
) : AdnlPacketReceiver, CoroutineScope, Closeable {
    override val coroutineContext: CoroutineContext = DISPATCHER
    private val startTime = Clock.System.now()
    private val localKey = AtomicReference(PrivateKeyEd25519())
    private val localId = AtomicReference(localKey.get().toAdnlIdShort())
    private val queries = ConcurrentMap<BitString, CompletableDeferred<AdnlMessageAnswer>>()
    private val receiverState = PeerState.receiver(startTime)
    private val senderState = PeerState.sender()
    private val socketAddress = InetSocketAddress(ipv4(address.ip), address.port)
    private val job = launch {
        while (isActive) {
            receive.collectLatest { (adnlId, datagram) ->
                launch {
                    if (localId.get() == adnlId) {
                        receiveDatagram(datagram)
                    } else {
//                        println("expected: ${localId.get()}, actual: $adnlId")
                    }
                }
            }
        }
    }

    override fun receiveAdnlAnswer(message: AdnlMessageAnswer) {
        val key = BitString(message.query_id)
        val deferred = queries[key]
        deferred?.complete(message)
    }

    private fun receiveDatagram(datagram: Datagram) {
        val encryptedData = datagram.packet.readBytes()
        val decryptedData = localKey.get().decrypt(encryptedData)
        val packet = AdnlPacketContents.decodeBoxed(decryptedData)
        receivePacket(packet)
    }

    suspend fun <Q : TLFunction<Q, A>, A : TlObject<A>> query(
        query: Q,
        id: ByteArray = randomQueryId(),
        timeout: Duration = 1.seconds,
        maxAnswerSize: Long = DEFAULT_MTU
    ): A {
        val queryData = query.toByteArray()
        val answerData = rawQuery(queryData, id, timeout, maxAnswerSize)
        return query.resultTlCodec().decodeBoxed(answerData)
    }

    open suspend fun rawQuery(
        query: ByteArray,
        id: ByteArray = randomQueryId(),
        timeout: Duration = 5.seconds,
        maxAnswerSize: Long = DEFAULT_MTU
    ): ByteArray {
        val queryId = BitString(id)
        val queryMessage = AdnlMessageQuery(queryId, query)
        val deferred = CompletableDeferred<AdnlMessageAnswer>()
        queries[queryId] = deferred
        return try {
            sendQuery(queryMessage)
            withTimeout(timeout) {
                deferred.await().answer
            }
        } finally {
            queries.remove(queryId)
            setLocalKey(PrivateKeyEd25519())
        }
    }

    suspend fun sendQuery(query: AdnlMessageQuery) {
        sendMessages(null, query)
    }

    suspend fun sendMessages(
        channel: Channel?,
        vararg messages: AdnlMessage
    ) = sendMessages(
        channel = channel,
        messages = messages.toList()
    )

    suspend fun sendMessages(
        channel: Channel?,
        messages: List<AdnlMessage>,
    ) {
        val now = Clock.System.now()
        val addressList = AdnlAddressList(
            addrs = listOf(),
            version = now.epochSeconds.toInt(),
            reinit_date = startTime.epochSeconds.toInt(),
            expire_at = (now + ADDRESS_LIST_TIMEOUT).epochSeconds.toInt()
        )
        val packet = AdnlPacketContents(
            from = if (channel == null) localKey.get().publicKey() else null,
            messages = if (messages.size > 1) messages else null,
            message = if (messages.size == 1) messages.first() else null,
            address = addressList,
            seqno = senderState.ordinaryHistory.seqno++,
            confirm_seqno = receiverState.ordinaryHistory.seqno,
            reinit_date = if (channel == null) startTime.epochSeconds.toInt() else null,
            dst_reinit_date = if (channel == null) senderState.reinitDate.epochSeconds.toInt() else null,
        )
        sendPacket(packet, channel)
    }

    suspend fun sendPacket(packet: AdnlPacketContents, channel: Channel?) {
        val array = packet.toByteArray()
        val decodeBoxed = AdnlPacketContents.decodeBoxed(array)
        check(decodeBoxed == packet)

        val data = if (channel == null) {
            val handshake = AdnlHandshakePacket(
                packet.signed(localKey.get()),
                key
            )
            handshake.build()
        } else {
            ByteReadPacket(key.encrypt(packet.toByteArray()))
        }
        sendDatagram(data)
    }

    suspend fun sendDatagram(data: ByteReadPacket) {
        val datagram = Datagram(data, socketAddress)
        socket.send(datagram)
    }

    private fun setLocalKey(key: PrivateKeyEd25519) {
        localKey.set(key)
        localId.set(key.toAdnlIdShort())
    }

    override fun close() {
        job.cancel()
        queries.clear()
    }

    companion object {
        val DEFAULT_MTU = 1024L
        val ADDRESS_LIST_TIMEOUT: Duration = 1000.seconds
        private val DISPATCHER = newSingleThreadContext("adnl-peer-dispatcher")
        private val socket = aSocket(ActorSelectorManager(DISPATCHER)).udp().bind()
        private val receive = MutableSharedFlow<Pair<AdnlIdShort, Datagram>>()
        private val globalJob = CoroutineScope(DISPATCHER).launch {
            while (isActive) {
                val datagram = socket.receive()
                val adnlId = AdnlIdShort.decode(datagram.packet)
                receive.emit(adnlId to datagram)
            }
        }

        @JvmStatic
        fun randomQueryId(): ByteArray = Random.nextBytes(32)
    }
}
