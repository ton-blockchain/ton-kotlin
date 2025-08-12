package org.ton.kotlin.dht

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.*
import org.ton.kotlin.blockchain.ShardPublicOverlayId
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.dht.bucket.KBucketConfig
import org.ton.kotlin.tl.TL
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.test.Test

class DhtTest {
    val TON_MAINNET_MASTERCHAIN =
        ShardPublicOverlayId.masterchain("XplPz01CXAps5qeSWUtxcyBfdAo5zVb1N979KLSKD24=".decodeBase64Bytes())
    val TON_TESTNET_MASTERCHAIN =
        ShardPublicOverlayId.masterchain("Z+IKwYS54DmmJmesw/nAD5DzWadnOCMzee+kdgSYDOg=".decodeBase64Bytes())
    val EVERSCALE_MAINNET_MASTERCHAIN =
        ShardPublicOverlayId.masterchain("0nC4eylStbp9qnCq8KjDYb789NjS25L5ZA1UQwcIOOQ=".decodeBase64Bytes())
    val EVERSCALE_TESTNET_MASTERCHAIN =
        ShardPublicOverlayId.masterchain("2Q2lg3IWbHJo9q2YXv1j2lwsmBtTuiT/djB66WEUd3c=".decodeBase64Bytes())
    val VENOM_MAINNET_MASTERCHAIN =
        ShardPublicOverlayId.masterchain("ywj7H75tJ3PgbEeX+UNP3j0iR1x9imIIJJuQgrlCr8s=".decodeBase64Bytes())

    @Test
    fun foo() {
        val port = 3111
        val adnl = Adnl(
            aSocket(SelectorManager()).udp().let {
                runBlocking { it.bind(port = port) }
            }
        )
        val localNode = adnl.localNode(PrivateKeyEd25519.random())
        val peer = localNode.peer(
            AdnlIdFull(PublicKeyEd25519(Base64.decode("aE+qxa/64H2NfeaBhU9Ja2HrK8BT3VxDf2ETtywsGYM="))),
            AdnlAddress.Udp(-1062693108, 3278)
        )
//        val dhtPing = DhtFunction.Ping(123)
//        val dhtPong = TL.Boxed.decodeFromByteArray<DhtPong>(
//            runBlocking { peer.query(TL.Boxed.encodeToByteArray(dhtPing)) }
//        )
//        println("DhtPong: $dhtPong")
        val result = TL.Boxed.decodeFromByteArray(
            DhtFunction.GetSignedAddressList.answerSerializer,
            runBlocking { peer.query(TL.Boxed.encodeToByteArray(DhtFunction.GetSignedAddressList)) }
        )
        println("DhtNode: $result")
    }

    val testNode = DhtNode(
        id = AdnlIdFull(PublicKeyEd25519("684faac5affae07d8d7de681854f496b61eb2bc053dd5c437f6113b72c2c1983".hexToByteArray())),
        addrList = AdnlAddressList(
            listOf(InetSocketAddress("192.168.151.12", 3278).toAdnlAddress()),
            version = 1753012978,
            reinitDate = 1753012978,
            priority = 0,
            expireAt = 0,
        ),
        version = 1753013642,
        signature = "53adcf8daa3912e0fbb81a0ddd53a3a3fe17b7af37eb2beab435d49aef90e4d0ded13f424c86f9f17f7e45c5d610bbb7bfc5405d5079968b68902f7fef77e801".hexToByteString()
    )

    @Test
    fun generateKey() {
        val array = arrayOfNulls<PrivateKeyEd25519>(1024)

        fun generateKey() {
            val key = PrivateKeyEd25519(Random.nextBytes(32))
            val shortId = key.computeShortId()
            // index by first 10 bits of shortId
            val b1 = shortId[0].toInt()
            val b2 = shortId[1].toInt()
            val index = ((b1 and 0xFF) shl 2) or ((b2 and 0xC0) shr 6)
            array[index] = key
        }

        val jpb = GlobalScope.launch {
            repeat(16) {
                launch {
                    while (isActive) {
                        repeat(10_000) {
                            generateKey()
                        }
                    }
                }
            }
        }

        while (true) {
            val count = array.count { it == null }
            println("Remaining keys: $count")
            if (count == 0) {
                println("All keys generated!")
                println("Keys:")
                for (i in array.indices) {
                    val key = array[i]
                    if (key != null) {
                        println("Key[$i]: ${key.computeShortId().toHexString()}")
                    }
                }
                jpb.cancel()
                break
            }
            runBlocking {
                delay(1000L)
            }
        }
    }

    @Test
    fun dhtTest() {
        val port = 3111
        val adnl = Adnl(
            aSocket(SelectorManager()).udp().let {
                runBlocking { it.bind(port = port) }
            }
        )
        println(testNode.id.shortId.hash.toByteArray().encodeBase64())
        val localNode = adnl.localNode(PrivateKeyEd25519.random())
        val dht = DhtLocalNode(
            localNode, config = DhtConfig(
                kBucketConfig = KBucketConfig(bucketSize = 20)
            )
        )
        for (node in DhtLocalNode.BOOTSTRAP_NODES) {
            dht.addNode(dht.peer(node))
        }

        runBlocking {
            dht.storeValue(
                DhtValue(
                    key = DhtKeyDescription(
                        key = DhtKey(
                            id = ByteString(Random.nextBytes(32)),
                            name = "test".encodeToByteString(),
                        ),
                        id = localNode.id.publicKey
                    ).signed(localNode.key),
                    value = "hello".encodeToByteString(),
                    ttl = 3600
                ).signed(localNode.key)
            )
        }

//        val overlayId = TON_MAINNET_MASTERCHAIN.overlayId.shortId()
//        val dhtKey = DhtKey(overlayId.publicKeyHash, "nodes".encodeToByteString(), 0)
//        fun HashMap<DhtPeer, Long>.distanceStats() =
//            keys.groupBy { it.key.distance(dhtKey).ilog2() ?: 0 }.entries.sortedBy { it.key }
//        runBlocking {
//            val nodes = HashMap<AdnlIdFull, OverlayNodeInfo>()
//            val foundStats = HashMap<DhtPeer, Long>()
//            val notFoundStats = HashMap<DhtPeer, Long>()
//            val failureStats = HashMap<DhtPeer, Long>()
//            val uniqueStats = HashMap<DhtPeer, Long>()
//            while (true) {
//                println("start finding overlay nodes...")
//                dht.findOverlayNodesResults(overlayId).collect { (peer, result) ->
//                    result.onSuccess { list ->
//                        if (list == null) {
//                            notFoundStats[peer] = (notFoundStats[peer] ?: 0) + 1
//                        } else {
//                            foundStats[peer] = (foundStats[peer] ?: 0) + 1
//                            list.forEach { nodeInfo ->
//                                if (nodes.put(nodeInfo.id, nodeInfo) == null) {
//                                    println("[${nodes.size}] Found overlay node: $nodeInfo")
//                                    uniqueStats[peer] = (uniqueStats[peer] ?: 0) + 1
//                                }
//                            }
//                        }
//                    }.onFailure {
//                        failureStats[peer] = (failureStats[peer] ?: 0) + 1
//                    }
//                }
//                println("Finished finding overlay nodes. Total count: ${nodes.size}")
//                println("DHT nodes with FOUND: ${foundStats.size}")
//                foundStats.distanceStats().forEach { (distance, stats) ->
//                    println("$distance: ${stats.size.toString().padEnd(4, ' ')} nodes ${"|".repeat(stats.size)}")
//                }
//                println("DHT nodes with NOT_FOUND: ${notFoundStats.size}")
//                notFoundStats.distanceStats().forEach { (distance, stats) ->
//                    println("$distance: ${stats.size.toString().padEnd(4, ' ')} nodes ${"|".repeat(stats.size)}")
//                }
//                println("DHT nodes with FAILURE: ${failureStats.size}")
//                failureStats.distanceStats().forEach { (distance, stats) ->
//                    println("$distance: ${stats.size.toString().padEnd(4, ' ')} nodes ${"|".repeat(stats.size)}")
//                }
//                println("DHT nodes with UNIQUE: ${uniqueStats.size}")
//                uniqueStats.distanceStats().forEach { (distance, stats) ->
//                    println("$distance: ${stats.size.toString().padEnd(4, ' ')} nodes ${"|".repeat(stats.size)}")
//                }
//            }
//        }

    }


}
