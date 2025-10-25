import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.ton.sdk.toncenter.client.TonCenterV3Client

class TonCenterTest {
    val client = TonCenterV3Client(HttpClient(CIO))

//    @Test
//    @Ignore
//    fun accountStates() {
//        val result = runBlocking {
//            client.accountStates(TonCenterAccountRequest {
//                address += AddressStd.parse("Ef8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAU")
//                address += AddressStd.parse("0:0ab558f4db84fd31f61a273535c670c091ffc619b1cdbbe5769a0bf28d3b8fea")
//            })
//        }
//        println(result)
//    }
//
//    @Test
//    fun walletStates() {
//        val result = runBlocking {
//            client.walletStates(TonCenterAccountRequest {
//                address += AddressStd.parse("Ef8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAU")
//                address += AddressStd.parse("0:0ab558f4db84fd31f61a273535c670c091ffc619b1cdbbe5769a0bf28d3b8fea")
//            })
//        }
//        println(result)
//    }
//
//    @Test
//    fun runGetMethod() {
//        val result = runBlocking {
//            client.runGetMethod(
//                TonCenterRunGetMethodRequest(
//                    address = AddressStd.parse("UQAKtVj024T9MfYaJzU1xnDAkf_GGbHNu-V2mgvyjTuP6uYH"),
//                    method = "seqno",
//                )
//            )
//        }
//        println(result)
//    }
//
//    @Test
//    fun masterchainInfo() {
//        val result = runBlocking {
//            client.masterchainInfo()
//        }
//        println(result)
//    }
//
//    @Test
//    fun blocks() {
//        val result = runBlocking {
//            val masterchainInfo = client.masterchainInfo()
//            client.blocks {
//                workchain = 0
//                mcSeqno = masterchainInfo.last.seqno
//            }
//        }
//        println(result)
//    }


}
