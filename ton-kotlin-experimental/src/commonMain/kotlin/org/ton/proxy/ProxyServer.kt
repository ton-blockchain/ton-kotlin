package org.ton.proxy

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.ton.kotlin.adnl.liteclient.config.LiteClientConfigGlobal
import org.ton.kotlin.lite.client.LiteClient
import org.ton.proxy.adnl.engine.CIOAdnlNetworkEngine
import org.ton.proxy.dht.Dht
import org.ton.proxy.dns.DnsResolver
import org.ton.proxy.rldp.Rldp

class ProxyServer(
    configGlobal: LiteClientConfigGlobal
) {
    val dht = Dht.lite(configGlobal.dht.static_nodes)
    val liteClient = LiteClient(configGlobal)
    val dnsResolver = DnsResolver(liteClient)
    val rldp = Rldp(CIOAdnlNetworkEngine(), dht)
    val httpClient = HttpClient(io.ktor.client.engine.cio.CIO) {
        followRedirects = true
    }

    val app = embeddedServer(CIO, port = 8080) {
        intercept(ApplicationCallPipeline.Call) {
            val channel = call.request.receiveChannel()
            val size = channel.availableForRead
            val buffer = ByteArray(size)
            channel.readFully(buffer)

            val host = call.request.host()

            httpClient.request("http://${call.request.host()}${call.request.uri}")
            call.request.headers
            call.request.httpMethod


        }

        routing {

        }
    }


}
