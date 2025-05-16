package org.ton.kotlin.adnl.connection

import org.ton.kotlin.adnl.network.IPAddress
import org.ton.kotlin.adnl.network.TcpClient
import org.ton.kotlin.adnl.network.TcpClientImpl
import org.ton.kotlin.api.liteserver.LiteServerDesc

public class AdnlConnectionFactory {
    public suspend fun connect(
        liteServerDesc: LiteServerDesc
    ): TcpClient {
        try {
            return TcpClientImpl().also {
                val address = IPAddress.ipv4(liteServerDesc.ip, liteServerDesc.port)
                it.connect(address.host, address.port)
            }
        } catch (cause: Throwable) {
            throw cause
        }
    }
}
