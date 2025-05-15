package org.ton.proxy.adnl

import org.ton.kotlin.adnl.adnl.AdnlPacketContents

interface AdnlPacketReceiver : AdnlMessageReceiver {
    fun receivePacket(packet: AdnlPacketContents) {
        packet.messages().forEach {
            receiveAdnlMessage(it)
        }
    }
}
