package org.ton.proxy.rldp

import org.ton.kotlin.adnl.rldp.RldpMessagePart

interface RldpReceiver {
    fun receiveRldpMessagePart(message: RldpMessagePart)
}
