import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.junit.jupiter.api.assertDoesNotThrow
import org.ton.sdk.toncenter.client.TonCenterV3Client
import org.ton.sdk.toncenter.model.TonCenterTransactionsResponse
import kotlin.test.Test

class TransactionsTest {
    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        namingStrategy = JsonNamingStrategy.SnakeCase
        decodeEnumsCaseInsensitive = true
    }
    val client = TonCenterV3Client.create()

    @Test
    fun transactionsParseTest() {
        val transaction = this::class.java.getResource("/transactions.json")!!.readText()
        assertDoesNotThrow {
            json.decodeFromString(
                TonCenterTransactionsResponse.serializer(),
                transaction
            )
        }
    }
}
