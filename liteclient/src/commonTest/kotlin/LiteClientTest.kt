import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.liteclient.TcpConnection
import kotlin.test.Test

class LiteClientTest {
    @Test
    fun foo() {
        val connection = TcpConnection(
            runBlocking {
                aSocket(SelectorManager()).tcp().connect("localhost", 4242).connection()
            },
            PublicKeyEd25519("HVqXt+cYSFyb5oxVF9KybJVQ3ItcJCtGgoFAHN4173s=".decodeBase64Bytes())
        )
        runBlocking {
            connection.handshake()
        }
    }
}
