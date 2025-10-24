import kotlinx.coroutines.runBlocking
import kotlinx.io.bytestring.hexToByteString
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.provider.liteapi.LiteApiClientImpl
import org.ton.kotlin.provider.liteapi.model.LiteServerDesc
import kotlin.test.Test

class LiteClientTest {
    @Test
    fun testClient() {
        val client = LiteApiClientImpl(
            LiteServerDesc(
                PublicKeyEd25519("1ba70d02beb05c1041c960f375d1163f9c4c16c01ca7acb5ddf5c0f10ec42653".hexToByteString()),
                -1185526007,
                4701
            )
        )
        val a = runBlocking {
            client.getMasterchainInfo()
        }
        println(a)
    }
}
