import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.ton.kotlin.adnl.Adnl
import org.ton.kotlin.adnl.AdnlAddress
import org.ton.kotlin.adnl.AdnlNode
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.dht.DhtLocalNode
import org.ton.kotlin.http.HttpLocalNode
import org.ton.kotlin.http.RldpClientHttpEngine
import org.ton.kotlin.rldp.RldpLocalNode
import kotlin.test.Test

class HttpTest {
    @Test
    fun httpTest() {
        val port = 3111
        val adnl = Adnl(
            aSocket(SelectorManager()).udp().let {
                runBlocking { it.bind(port = port) }
            }
        )
        val key = PrivateKeyEd25519.random()
        val adnlLocalNode = adnl.localNode(key)
        val rldpLocalNode = RldpLocalNode(adnlLocalNode)
        val httpLocalNode = HttpLocalNode(rldpLocalNode)
        val dhtLocalNode = DhtLocalNode(adnlLocalNode)
        DhtLocalNode.BOOTSTRAP_NODES.forEach {
            dhtLocalNode.addNode(dhtLocalNode.peer(it))
        }

        val httpEngine = RldpClientHttpEngine(
            httpLocalNode,
            adnlAddressResolver = { resolving ->
                AdnlNode(
                    PrivateKeyEd25519("cXsehYQXlpakeyJV2Kde/OnKsz/m0cO3jZ5NoMIUasY=".decodeBase64Bytes()).publicKey(),
                    AdnlAddress.Udp("192.168.151.12", 13167)
                ).let {
                    if (it.shortId == resolving) {
                        return@RldpClientHttpEngine it
                    }
                }

                dhtLocalNode.resolveAddress(resolving)
            }
        )


        val httpClient = HttpClient(httpEngine)
        val result = runBlocking {
            httpClient.get("http://U7ERDJ5QOLIOTWPFEDIQCK73FGAGMTYKM6ZI2B5VPGF2XHPKMEGCRTZ.adnl/")
        }
        println("result = $result")
        val body = runBlocking {
            result.bodyAsText()
        }
        println("body: ${body.take(500)}")
    }
}
