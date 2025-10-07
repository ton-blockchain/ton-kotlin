package org.ton.api.http.server

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlIdShort
import org.ton.tl.*

@SerialName("http.server.dnsEntry")
@Serializable
public data class HttpServerDnsEntry(
    val domain: String,
    val addr: AdnlIdShort
) {
    public companion object : TlCodec<HttpServerDnsEntry> by HttpServerDnsEntryTlConstructor
}

private object HttpServerDnsEntryTlConstructor : TlConstructor<HttpServerDnsEntry>(
    schema = "http.server.dnsEntry domain:string addr:adnl.id_short = http.server.DnsEntry"
) {
    override fun decode(input: TlReader): HttpServerDnsEntry {
        val domain = input.readString()
        val addr = input.read(AdnlIdShort)
        return HttpServerDnsEntry(domain, addr)
    }

    override fun encode(output: TlWriter, value: HttpServerDnsEntry) {
        output.writeString(value.domain)
        output.write(AdnlIdShort, value.addr)
    }
}
