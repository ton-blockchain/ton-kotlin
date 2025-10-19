import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.ton.kotlin.provider.toncenter.TonCenterV3Client
import org.ton.kotlin.provider.toncenter.model.TonCenterAccountStatesRequest
import org.ton.kotlin.provider.toncenter.model.TonCenterRunGetMethodRequest
import kotlin.test.Test

class TonCenterTest {
    @Test
    fun masterchainInfo() {
        val client = TonCenterV3Client(HttpClient(CIO))
//        println(client.masterchainInfoAsync().get())

        println(
            client.runGetMethodAsync(
                TonCenterRunGetMethodRequest(
                    address = "UQAKtVj024T9MfYaJzU1xnDAkf_GGbHNu-V2mgvyjTuP6uYH",
                    method = "seqno",
                    stack = listOf()
                )
            ).get()
        )

        val result = client.accountStatesAsync(
            TonCenterAccountStatesRequest.builder()
                .address("UQAKtVj024T9MfYaJzU1xnDAkf_GGbHNu-V2mgvyjTuP6uYH")
                .includeBoc(true)
                .build()
        ).get()

        println(result)
    }
}
