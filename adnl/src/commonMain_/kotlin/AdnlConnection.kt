package org.ton.kotlin.adnl

import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readByteArray
import org.ton.kotlin.adnl.transport.AdnlReadWriteChannel
import org.ton.kotlin.adnl.transport.AdnlTransport
import org.ton.kotlin.crypto.Decryptor
import org.ton.kotlin.crypto.Encryptor
import kotlin.coroutines.CoroutineContext

class AdnlConnectionManager() {
    val connections = hashMapOf<AdnlIdShort, AdnlConnection>()

    fun getConnection(id: AdnlIdShort): AdnlConnection? {
        return connections[id]
    }

    fun registerConnection(
        id: AdnlIdShort,
        connection: AdnlConnection
    ) {
        connections.put(id, connection)?.cancel()
    }

    fun unregisterConnection(
        id: AdnlIdShort
    ) {
        connections.remove(id)?.cancel()
    }
}

class AdnlConnection(
    val encryptor: Encryptor,
    val decryptor: Decryptor,
//    val transport: AdnlTransport,
) : AdnlReadWriteChannel, CoroutineScope {
    private val sendChannel = Channel<AdnlPacket>()
    private val receiveChannel = Channel<AdnlPacket>()
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    init {
        launch {
            while (true) {
                select {
                    sendChannel.onReceive {
                        processOutgoingPacket(it)
                    }
//                    transport.incoming.onReceive {
//                        processIncomingDatagram(it)
//                    }
                }
            }
        }.invokeOnCompletion {
            sendChannel.close(it)
            receiveChannel.close(it)
        }
    }

    override val incoming: ReceiveChannel<AdnlPacket>
        get() = receiveChannel

    override val outgoing: SendChannel<AdnlPacket>
        get() = sendChannel

    private suspend fun processIncomingDatagram(datagram: Source) {
        val decrypted = decryptor.decrypt(datagram.readByteArray())
        val packet = AdnlPacket.deserialize(decrypted)
        receiveChannel.send(packet)
    }

    private suspend fun processOutgoingPacket(packet: AdnlPacket) {
        val payload = AdnlPacket.serialize(packet)
        val encrypted = encryptor.encrypt(payload)
//        transport.send(Buffer().apply { writeFully(encrypted) })
    }
}
