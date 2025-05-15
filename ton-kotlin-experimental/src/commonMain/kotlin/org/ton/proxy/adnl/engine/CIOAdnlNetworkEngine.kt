package org.ton.proxy.adnl.engine

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.newFixedThreadPoolContext
import org.ton.kotlin.adnl.ipv4
import org.ton.kotlin.adnl.utils.toAdnlUdpAddress
import org.ton.kotlin.adnl.utils.toSocketAddress
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp

class CIOAdnlNetworkEngine(
    localAddress: AdnlAddressUdp = AdnlAddressUdp(ipv4("0.0.0.0"), 0)
) : AdnlNetworkEngine {
    val socket = aSocket(ActorSelectorManager(DISPATCHER)).udp().bind(localAddress.toSocketAddress())

    override suspend fun sendDatagram(adnlAddress: AdnlAddressUdp, payload: ByteReadPacket) {
        val datagram = Datagram(payload, adnlAddress.toSocketAddress())
        socket.send(datagram)
    }

    override suspend fun receiveDatagram(): Pair<AdnlAddressUdp, ByteReadPacket> {
        val datagram = socket.receive()
        val adnlAddress = datagram.address.toAdnlUdpAddress()
        val payload = datagram.packet
        return adnlAddress to payload
    }

    companion object {
        val DISPATCHER = newFixedThreadPoolContext(1, "CIOAdnlNetworkEngine")
    }
}
