package org.ton.kotlin.adnl.engine

import io.ktor.utils.io.core.*
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp

public interface AdnlNetworkEngine {
    public suspend fun sendDatagram(adnlAddress: AdnlAddressUdp, payload: ByteReadPacket)
    public suspend fun receiveDatagram(): Pair<AdnlAddressUdp, ByteReadPacket>
}
