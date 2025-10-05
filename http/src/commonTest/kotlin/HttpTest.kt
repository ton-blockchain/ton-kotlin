import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.runBlocking
import org.ton.kotlin.adnl.Adnl
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.dht.DhtLocalNode
import org.ton.kotlin.http.HttpLocalNode
import org.ton.kotlin.http.RldpClientHttpEngine
import org.ton.kotlin.rldp.RldpLocalNode
import kotlin.test.Test

class HttpTest {
    @Test
    fun httpTest() {
        val socket = aSocket(SelectorManager()).udp().let { runBlocking { it.bind(port = 3111) } }
        val key = PrivateKeyEd25519.random()
        val adnl = Adnl(socket).localNode(key)
        val dht = DhtLocalNode(adnl)
        val rldp = RldpLocalNode(adnl)
        val http = HttpLocalNode(rldp)

        DhtLocalNode.BOOTSTRAP_NODES.forEach {
            dht.addNode(dht.peer(it))
        }

        val httpClient = HttpClient(RldpClientHttpEngine(http, dht))
        val result = runBlocking {
            httpClient.get("http://U7ERDJ5QOLIOTWPFEDIQCK73FGAGMTYKM6ZI2B5VPGF2XHPKMEGCRTZ.adnl/") // getting-started.ton
        }
        println("================= NEW HTTP REQUEST ==================")
        println("result = $result")
        val body = runBlocking {
            result.bodyAsText()
        }
        println("body: ${body.take(500)}")
    }
}
