package org.ton.kotlin.http

import io.ktor.client.engine.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import org.ton.kotlin.dht.DhtLocalNode

class RldpClientHttpEngine(
    private val httpLocalNode: HttpLocalNode,
    private val dhtLocalNode: DhtLocalNode,
    override val config: HttpClientEngineConfig = HttpClientEngineConfig(),
) : HttpClientEngineBase("rldp") {
    @InternalAPI
    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        data.url.host
        TODO()
    }

}
