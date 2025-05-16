package org.ton.kotlin.adnl.utils

import io.ktor.network.sockets.*
import org.ton.kotlin.adnl.ipv4
import org.ton.kotlin.api.adnl.AdnlAddressUdp

public fun AdnlAddressUdp.toSocketAddress(): InetSocketAddress = InetSocketAddress(ipv4(ip), port)
public fun InetSocketAddress.toAdnlUdpAddress(): AdnlAddressUdp {
    return if (hostname.count { it == '.' } == 3) {
        AdnlAddressUdp(ipv4(hostname), port)
    } else {
        AdnlAddressUdp(0, port)
    }
}

public fun SocketAddress.toAdnlUdpAddress(): AdnlAddressUdp = (this as InetSocketAddress).toAdnlUdpAddress()
