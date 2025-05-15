import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.ton.kotlin.lite.client.LiteClient
import kotlin.test.Test

class LiteClientIntegrationTest {
    @Test
    fun integrationTestGetLastBlockId() = runBlocking {
        val liteClient = LiteClient(Dispatchers.Default, CONFIG)
        println(liteClient.getLastBlockId())
    }
}
