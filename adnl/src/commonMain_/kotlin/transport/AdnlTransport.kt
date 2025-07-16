package org.ton.kotlin.adnl.transport

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.io.Buffer
import org.ton.kotlin.adnl.AdnlPacket

interface AdnlPacketWriteChannel {
    val outgoing: SendChannel<AdnlPacket>

    suspend fun send(packet: AdnlPacket) = outgoing.send(packet)
}

interface AdnlPacketReadChannel {
    val incoming: ReceiveChannel<AdnlPacket>

    suspend fun receive(): AdnlPacket = incoming.receive()
}

interface AdnlReadWriteChannel : AdnlPacketReadChannel, AdnlPacketWriteChannel

interface AdnlTransport<Address> {
    val localAddress: Address

    val incoming: ReceiveChannel<Buffer>
    val outgoing: SendChannel<Pair<Buffer, Address>>

    suspend fun send(source: Buffer, destination: Address) = outgoing.send(source to destination)

    suspend fun receive(): Buffer = incoming.receive()
}
