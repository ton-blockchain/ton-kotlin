package org.ton.adnl.engine

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.io.Source
import org.ton.adnl.ipv4
import org.ton.adnl.utils.toAdnlUdpAddress
import org.ton.adnl.utils.toSocketAddress
import org.ton.api.adnl.AdnlAddressUdp

public class CIOAdnlNetworkEngine(
    localAddress: AdnlAddressUdp = AdnlAddressUdp(ipv4("0.0.0.0"), 0)
) : AdnlNetworkEngine {
    public constructor(port: Int) : this(AdnlAddressUdp(ipv4("0.0.0.0"), port))

    public val socket: BoundDatagramSocket =
        aSocket(ActorSelectorManager(DISPATCHER)).udp().let {
            runBlocking {
                it.bind(localAddress.toSocketAddress())
            }
        }

    override suspend fun sendDatagram(adnlAddress: AdnlAddressUdp, payload: Source) {
        val datagram = Datagram(payload, adnlAddress.toSocketAddress())
        socket.send(datagram)
    }

    override suspend fun receiveDatagram(): Pair<AdnlAddressUdp, Source> {
        val datagram = socket.receive()
        val adnlAddress = datagram.address.toAdnlUdpAddress()
        val payload = datagram.packet
        return adnlAddress to payload
    }

    public companion object {
        @OptIn(DelicateCoroutinesApi::class)
        private val DISPATCHER = newFixedThreadPoolContext(1, "CIOAdnlNetworkEngine")
    }
}
