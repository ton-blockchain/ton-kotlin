package org.ton.proxy.adnl.engine

import io.ktor.utils.io.core.*
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp

interface AdnlNetworkEngine {
    suspend fun sendDatagram(adnlAddress: AdnlAddressUdp, payload: ByteReadPacket)
    suspend fun receiveDatagram(): Pair<AdnlAddressUdp, ByteReadPacket>
}
