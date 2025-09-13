import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.bytestring.hexToByteString
import org.ton.kotlin.adnl.Adnl
import org.ton.kotlin.adnl.AdnlAddress
import org.ton.kotlin.adnl.AdnlNode
import org.ton.kotlin.blockchain.ShardPublicOverlayId
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.dht.DhtLocalNode
import org.ton.kotlin.overlay.OverlayLocalNode
import org.ton.kotlin.rldp.RldpLocalNode
import org.ton.kotlin.storage.Torrent
import kotlin.random.Random
import kotlin.test.Test

class TorrentTest {
    @Test
    fun torrentTest(): Unit = runBlocking {
        val adnl = Adnl(aSocket(SelectorManager()).udp().bind()).localNode(PrivateKeyEd25519.random(Random(123)))
        println("our id: ${adnl.shortId}")
        val dht = DhtLocalNode(adnl)
        DhtLocalNode.BOOTSTRAP_NODES.forEach {
            dht.addNode(dht.peer(it))
        }
//        val node = DhtNode(
//            PublicKeyEd25519("oOgREq4vf7NIZ50wFndSUfpwWwm6b6oE88qK9LOXPjk=".decodeBase64Bytes()),
//            addressList = AdnlAddressList(
//                AdnlAddress.Udp("192.168.151.12", 3278)
//            )
////        )
//        println(node.id.shortId.hash.toByteArray().encodeBase64())
//        dht.addNode(dht.peer(node))


        val goStorageKey = PublicKeyEd25519(
            "fmFDoGu0EK8hYMNGGBoHGVxdPBX3aQSupe1ngS3NA8WtNmtKR4e6Rghp8N70AWR9z5gzMiskjLDoIPfBwIa5dw==".decodeBase64Bytes()
                .copyOfRange(32, 64)
        )
        val rldp = RldpLocalNode(adnl)
        val overlays = OverlayLocalNode(adnl, dht, adnlNodeResolver = {
            if (it.hash == goStorageKey.computeShortId()) {
                val node = AdnlNode(
                    goStorageKey,
                    AdnlAddress.Udp("192.168.151.12", 17556)
                )
                node
            } else {
                dht.resolveAdnlNode(it)
            }
        })

        ShardPublicOverlayId.masterchain("eh9yveSz1qMdJ7mOsO+I+H77jkLr9NpAuEkoJuseXBo=".decodeBase64Bytes()).overlayId
        ShardPublicOverlayId.masterchain("0nC4eylStbp9qnCq8KjDYb789NjS25L5ZA1UQwcIOOQ=".decodeBase64Bytes())
        ShardPublicOverlayId.masterchain("XplPz01CXAps5qeSWUtxcyBfdAo5zVb1N979KLSKD24=".decodeBase64Bytes()).overlayId

//        val overlay = Overlay(coroutineContext, EVERSCALE_MAINNET_MASTERCHAIN.overlayId, OverlayType.Public, adnl = localNode, dht = dht)
//

        val torrent = Torrent(
//            hash = "7FEA7AF2325F0A5B6908939C9D72F92DB4C0CA52CCA8CE2C48BB2708BF188541".hexToByteString(),
//            hash = "d9bdaf23d506e75259687d4d8369ec475198f750a8fad94eb0ed4973e67c95c1".hexToByteString(),
            hash = "20A1E61CB22D3BE753C9D7BF8DAE996EECE168A2DC2CB50B758E2BD3E79A296E".hexToByteString(),
            rldp = rldp,
            overlays = overlays,
        )
        val torrentInfo = torrent.torrentInfo.await()
        println("torrentInfo: $torrentInfo")
        val header = torrent.header.await()
        println("header: $header")
//        delay(Long.MAX_VALUE)

        // peer: 02fbba5b982df1171e5d0736234bafc0235e3076cd414fd0734e1ca6abbe2d76
        // out:
    }
}
