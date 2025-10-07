package org.ton.adnl.engine

import kotlinx.io.Source
import org.ton.api.adnl.AdnlAddressUdp

public interface AdnlNetworkEngine {
    public suspend fun sendDatagram(adnlAddress: AdnlAddressUdp, payload: Source)
    public suspend fun receiveDatagram(): Pair<AdnlAddressUdp, Source>
}
