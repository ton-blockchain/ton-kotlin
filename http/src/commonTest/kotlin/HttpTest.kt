import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.ton.kotlin.adnl.Adnl
import org.ton.kotlin.adnl.AdnlAddress
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.http.HttpLocalNode
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

        val connection = httpLocalNode.connection(
            AdnlIdFull(PrivateKeyEd25519("cXsehYQXlpakeyJV2Kde/OnKsz/m0cO3jZ5NoMIUasY=".decodeBase64Bytes())),
            AdnlAddress.Udp("192.168.151.12", 13167)
        )
        runBlocking {
            val (response, body) = connection.request("GET", "/")
            println("response: $response")
            body.collect {
                println("part: $it")
            }
        }
    }
}
