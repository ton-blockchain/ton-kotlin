import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.runBlocking
import org.ton.kotlin.adnl.Adnl
import org.ton.kotlin.crypto.PrivateKeyEd25519
import kotlin.test.Test

class AdnlNodeTest {
    @Test
    fun create() = runBlocking {
        val adnl = Adnl(aSocket(SelectorManager()).udp().bind())
        adnl.localNode(PrivateKeyEd25519.random()) {
            onMessage { message ->

            }
            onMessage {

            }
        }
    }
}
