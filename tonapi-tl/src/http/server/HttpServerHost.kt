package org.ton.api.http.server

import kotlinx.serialization.SerialName
import org.ton.api.adnl.AdnlIdShort
import org.ton.tl.*

public data class HttpServerHost(
    val domains: Collection<String>,
    val ip: Int,
    val port: Int,
    @SerialName("adnl_id")
    val adnlId: AdnlIdShort
) {
    public companion object : TlCodec<HttpServerHost> by HttpServerHostTlConstructor
}

private object HttpServerHostTlConstructor : TlConstructor<HttpServerHost>(
    schema = "http.server.host domains:(vector string) ip:int32 port:int32 adnl_id:adnl.id.short = http.server.Host"
) {
    override fun decode(input: TlReader): HttpServerHost {
        val domains = input.readVector {
            readString()
        }
        val ip = input.readInt()
        val port = input.readInt()
        val adnl_id = input.read(AdnlIdShort)
        return HttpServerHost(domains, ip, port, adnl_id)
    }

    override fun encode(output: TlWriter, value: HttpServerHost) {
        output.writeVector(value.domains) {
            writeString(it)
        }
        output.writeInt(value.ip)
        output.writeInt(value.port)
        output.write(AdnlIdShort, value.adnlId)
    }
}
