package org.ton.experimental

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.ton.kotlin.adnl.ipv4
import org.ton.kotlin.adnl.adnl.AdnlAddress
import org.ton.kotlin.adnl.adnl.AdnlAddressUdp
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.adnl.AdnlPacketContents
import org.ton.kotlin.adnl.dht.DhtNode
import org.ton.kotlin.adnl.dht.DhtNodes
import org.ton.kotlin.adnl.http.HttpHeader
import org.ton.kotlin.adnl.http.HttpResponse
import org.ton.kotlin.adnl.http.functions.HttpGetNextPayloadPart
import org.ton.kotlin.adnl.http.functions.HttpRequest
import org.ton.kotlin.adnl.liteclient.config.LiteClientConfigGlobal
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.block.DnsAdnlAddress
import org.ton.kotlin.crypto.encodeHex
import org.ton.kotlin.crypto.hex
import org.ton.kotlin.lite.client.LiteClient
import org.ton.proxy.adnl.engine.CIOAdnlNetworkEngine
import org.ton.proxy.dht.Dht
import org.ton.proxy.dns.DnsCategory
import org.ton.proxy.dns.DnsResolver
import org.ton.proxy.rldp.Rldp
import java.io.File
import kotlin.random.Random
import kotlin.test.Test

class Test {

    @Test
    fun testPing() = runBlocking {
        val file = HttpClient(CIO).get("https://ton.org/global-config.json").readBytes().decodeToString()
        val json = Json {
            ignoreUnknownKeys = true
        }
        val config = json.decodeFromString<LiteClientConfigGlobal>(file)
        val liteClient = LiteClient(config)
        val dns = DnsResolver(liteClient)
//        val siteKey = PublicKeyEd25519(hex("f8ee48b91cfd7cdfa2f840e18cc1f54e6e790cd3200a403e5ba5ef88e295b55f"))
//        val siteAddr = siteKey.toAdnlIdShort()
//        val resolver = MapAdnlAddressResolver(mapOf(
//            siteAddr to (siteKey to listOf(AdnlAddressUdp(ipv4("127.0.0.1"), 3333)))
//        ))
        val dht = Dht.lite(config.dht.static_nodes)

        val host = "foundation.ton"
        val siteAddr = AdnlIdShort((dns.resolve(host, DnsCategory.SITE) as DnsAdnlAddress).adnl_addr)

        val rldp = Rldp(CIOAdnlNetworkEngine(), dht)
        println("addr: $siteAddr")

        val id2 = BitString(Random.nextBytes(32))
        val response2 = rldp.get(siteAddr, host, id2)
        println(json.encodeToString(response2))

        val payload = rldp.query(siteAddr, HttpGetNextPayloadPart(id2, 0, 1 shl 25))
        println(json.encodeToString(payload))
    }

    suspend fun Rldp.get(address: AdnlIdShort, host: String, id: BitString): HttpResponse {
        return query(
            address, HttpRequest(
                id = id,
                method = "GET",
                url = "http://$host",
                http_version = "HTTP/1.1",
                headers = listOf(
                    HttpHeader("Host", host),
                    HttpHeader("Connection", "Keep-Alive"),
                    HttpHeader("Accept", "*/*"),
                ),
            )
        )
    }

    private fun loadNodes() = Json.decodeFromString<DhtNodes>(File("dht.json").readText())

    @Test
    fun collectInfo() {
        var dhtNodes: List<DhtNode> = loadNodes()
        println("DHT Nodes: ${dhtNodes.size}")
        val addresses = HashSet<AdnlAddress>()
        dhtNodes.forEach { node ->
            node.addr_list.addrs.forEach { addr ->
                if (!addresses.add(addr)) {
//                    println("Duplicate address: $addr - $node")
                }
            }
        }
        println("Addresses: ${addresses.size}")
        addresses.sortedBy { (it as? AdnlAddressUdp)?.ip?.toUInt()?.toLong() ?: 0 }.forEach {
            if (it is AdnlAddressUdp) {
                println("${ipv4(it.ip)}:${it.port} - $it")
            } else {
                println("exotic: $it")
            }
        }
    }

    @Test
    fun testKeys() {
        val alicePrivate = PrivateKeyEd25519(ByteArray(32) { 0xFF.toByte() })
        val alicePublic = alicePrivate.publicKey()

        println("alice secret " + alicePrivate.key.encodeHex())
        println("alice public " + alicePublic.key.encodeHex())

        val bobPrivate = PrivateKeyEd25519(ByteArray(32) { 0x00.toByte() })
        val bobPublic = bobPrivate.publicKey()

        println("bob secret " + bobPrivate.key.encodeHex())
        println("bob public " + bobPublic.key.encodeHex())

        val original = "Hello, world!".toByteArray()
        val encrypted = alicePublic.encrypt(original)
        val decrypted = alicePrivate.decrypt(encrypted)

        println("Original: ${original.encodeHex()}")
        println("Decrypted: ${decrypted.encodeHex()}")
    }


    @Test
    fun packetContents() {
        val message =
            AdnlPacketContents.decodeBoxed(hex("89cd42d1073c306ea2a96d35d90c0000c6b413486ef4b8019c944ca848ace2f88a4f67dc08a8487d0ca11c9dd27a5659a185a2c6010000007af98bb400000000000000000000000000000000000000000000000000000000000000000c183febcb390500000000000000000001000000e7a60d670100007fce0c0000cde25f63cde25f6300000000b5e65f6300000000000000000000000000000000cde25f63ff1c74c840f45759eefb9c438532c6fe635f869290aee2b9324fbb46c569d05d5ee0f256df639030893be5977f01a2781d3c14bf9587d8129d8be5ad569cedf908b48b200100000007373585b0e7bb1b"))
    }
}
