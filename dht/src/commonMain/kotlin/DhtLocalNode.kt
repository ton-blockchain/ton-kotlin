package org.ton.kotlin.dht

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.selects.select
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.KSerializer
import org.ton.kotlin.adnl.*
import org.ton.kotlin.crypto.PublicKeyEd25519
import org.ton.kotlin.dht.bucket.Distance
import org.ton.kotlin.dht.bucket.KBucketConfig
import org.ton.kotlin.dht.bucket.KademliaRoutingTable
import org.ton.kotlin.dht.bucket.Key
import org.ton.kotlin.tl.TL
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.time.ExperimentalTime

/**
 * The `k` parameter of the DHT.
 *
 * This parameter determines:
 *   1. The (fixed) maximum number of nodes in a bucket.
 *   2. The (default) replication factor, which in turn determines:
 *      a) The number of closer peers returned in response to a request.
 *      b) The number of closest peers to a key to search for in an iterative query.
 *
 * The choice of (1) is fixed to this constant. The replication factor is configurable
 * but should generally be no greater than `K_VALUE`. All nodes in a Kademlia
 * DHT should agree on the choices made for (1) and (2).
 */
const val K_VALUE = 6

/**
 * The `a` parameter of the DHT.
 *
 * This parameter determines the default parallelism for iterative queries,
 * i.e. the allowed number of in-flight requests that an iterative query is
 * waiting for at a particular time while it continues to make progress towards
 * locating the closest peers to a key.
 */
const val ALPHA_VALUE = 3


interface Dht : AdnlNodeResolver {
    val routingTable: RoutingTable<DhtPeer>

    suspend fun findValues(key: Key): Flow<Pair<DhtPeer, Result<DhtValueResult>>>

    suspend fun findValue(key: Key): DhtValue?

    /**
     * Stores a value in the DHT, locally as well as at the nodes closest to the key as per the xor distance metric.
     */
    suspend fun storeValue(
        value: DhtValue,
        quorum: Quorum = Quorum.All,
    ): List<Pair<DhtPeer, Result<DhtStored>>>

    /**
     * Stores a value at specified nodes, without storing it in the local storage.
     */
    suspend fun storeValue(
        value: DhtValue,
        nodes: Iterable<DhtPeer>,
        quorum: Quorum = Quorum.All,
    ): List<Pair<DhtPeer, Result<DhtStored>>>

    fun getClosestLocalPeers(key: Key, source: AdnlIdShort): Sequence<DhtPeer> =
        routingTable.nearest(key)
            .filter { it.peerPair.remoteNode.shortId != source }

    override suspend fun resolveAdnlNode(adnlIdShort: AdnlIdShort): AdnlNode? {
        val value = findValue(DhtKey(adnlIdShort.hash, "address")) ?: return null
        val id = AdnlIdFull(value.key.id)
        require(id.shortId == adnlIdShort) {
            "AdnlIdShort mismatch: expected ${adnlIdShort}, got ${id.shortId}"
        }
        val addressList = TL.Boxed.decodeFromByteString<AdnlAddressList>(value.value)
        return AdnlNode(id, addressList)
    }
}

class DhtLocalNode(
    val localNode: AdnlLocalNode,
    val storage: DhtStorage = MemoryDhtStorage(),
    val config: DhtConfig = DhtConfig()
) : Dht {
    val localKey = DhtKeyId(localNode.shortId)
    override val routingTable = KademliaRoutingTable<DhtPeer>(localKey, config.kBucketConfig, keyForNode = { it.key })
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var triggerBootstrap = false
    private val job = coroutineScope.launch {
//        bootstrap(0)
        while (true) {
            delay(10_000)
//            bootstrap(Random.nextInt((256 - 16) until 256))
//            for (i in (256 - 16) until 256) {
//                bootstrap(i)
//                delay(10_000)
//            }
        }
    }

    fun peer(node: DhtNode) = DhtPeer(
        dht = this,
        peerPair = localNode.peer(node.toAdnlNode()),
        initialInfo = node
    )

    override suspend fun findValues(key: Key): Flow<Pair<DhtPeer, Result<DhtValueResult>>> {
        return query(
            target = key,
            query = { it.findValue(key.hash, K_VALUE) },
            newNodesExtractor = { peer, result ->
                result.nodesOrNull() ?: peer.findNode(key.hash, K_VALUE)
            }
        )
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun findValue(key: Key): DhtValue? {
        var value: DhtValue? = storage.findValue(DhtKeyId(key.hash))
        if (value != null) {
            return value
        }
        val notFound = ArrayList<Pair<DhtPeer, Distance>>()

        findValues(key).takeWhile { (peer, result) ->
            when (val valueResult = result.getOrNull()) {
                is DhtValueResult.Found -> {
                    if (valueResult.value.isExpired()) return@takeWhile true
                    value = valueResult.value
                    return@takeWhile false
                }

                is DhtValueResult.NotFound -> {
                    notFound.add(peer to peer.key.distance(key))
                }

                else -> {}
            }
            true
        }.collect()

        value?.let { value ->
            val (peer, _) = notFound.minWithOrNull { (_, d1), (_, d2) ->
                d1.compareTo(d2)
            } ?: return@let
//            println("not found: ${notFound.map { "${it.first.key} - ${it.second}" }}")
//            println("not found for peer: ${peer.key} - ${d} - $value")
            storeValue(value, listOf(peer), Quorum.One).forEach {
//                println("result of correcting: ${it}")
            }
        }

        return value
    }

    fun getClosestLocalPeers() = getClosestLocalPeers(localKey, localNode.shortId)

    fun addNode(peer: DhtPeer) {
        routingTable.add(peer)?.let { evictCandidate ->
            coroutineScope.launch {
                if (!evictCandidate.checkConnection()) {
                    routingTable.evict(evictCandidate)
                }
            }
        }
    }

    private suspend fun <R> query(
        target: Key,
        peers: Iterable<DhtPeer> = getClosestLocalPeers(target, localNode.shortId).asIterable(),
        query: suspend (DhtPeer) -> R,
        newNodesExtractor: suspend (DhtPeer, R) -> Iterable<DhtNode>?,
        a: Int = ALPHA_VALUE
    ): Flow<Pair<DhtPeer, Result<R>>> = channelFlow {
        coroutineScope {
            val queried = mutableSetOf<DhtKeyId>()
            val nodes = peers
                .mapTo(ArrayList()) { it.key.distance(target) to it }
//            println("starting queryMap with ${nodes.size} nodes - $query")
            val inFlight = mutableSetOf<Deferred<Pair<DhtPeer, Result<R>>>>()

            fun schedule() {
                nodes.asSequence()
                    .filter { (_, peer) -> peer.key !in queried }
                    .minWithOrNull { (d1, _), (d2, _) ->
                        d1.compareTo(d2)
                    }?.also { (_, peer) ->
                        queried += peer.key
                        inFlight += async {
//                            println("Querying peer: ${peer.key} {${peer.peerPair.remoteNode.publicKey}}")
                            peer to try {
                                Result.success(query(peer))
                            } catch (e: Throwable) {
//                                println("failed ${peer.key} {${peer.peerPair.remoteNode.publicKey}}: ${e.message}")
                                Result.failure(e)
                            }
                        }
                    }
            }

            repeat(a) {
                schedule()
            }
            while (inFlight.isNotEmpty()) {
                val result = select {
                    inFlight.forEach { d ->
                        d.onAwait { result ->
                            inFlight.remove(d)
                            result
                        }
                    }
                }
                val (peer, r) = result
                if (r.isSuccess) {
                    addNode(peer)
                }
                send(result)
                val newNodes = result.second.getOrNull()?.let {
                    runCatching {
                        newNodesExtractor(peer, it)
                    }.getOrNull()
                }
                if (newNodes != null) {
                    for (newNode in newNodes) {
                        val key = DhtKeyId(newNode.id)
                        if (key !in queried) {
                            val newPeer = peer(newNode)
                            val distance = key.distance(target)
                            nodes += distance to newPeer
                        }
                    }
                }
                schedule()
            }
            close()
        }
    }

    override suspend fun storeValue(value: DhtValue, quorum: Quorum) = coroutineScope {
        storage.storeValue(value)

        val closestNodes = HashMap<DhtKeyId, DhtPeer>()
        val closestLocalPeers = getClosestLocalPeers(value.key, localNode.shortId)
        closestLocalPeers.forEach {
            closestNodes[it.key] = it
        }
        findNodes(
            target = value.key,
            peers = closestLocalPeers.asIterable()
        ).take(K_VALUE).collect { (peer, result) ->
            result.getOrNull()?.let { nodes ->
                closestNodes[peer.key] = peer
                nodes.forEach { info ->
                    val key = DhtKeyId(info.id)
                    val oldPeer = closestNodes[key]
                    if (oldPeer == null) {
                        closestNodes[key] = peer(info)
                    }
                }
            }
        }

        val sortedClosestNodes = closestNodes
            .asSequence()
            .sortedBy { it.key.distance(value.key) }
            .map { it.value }
            .asIterable()

        storeValue(value, sortedClosestNodes, quorum)
    }

    override suspend fun storeValue(
        value: DhtValue,
        nodes: Iterable<DhtPeer>,
        quorum: Quorum
    ): List<Pair<DhtPeer, Result<DhtStored>>> = coroutineScope {
        val results = nodes
            .asSequence()
            .take(quorum.eval(config.replicationFactor))
            .map { peer ->
                async {
                    peer to runCatching {
                        peer.store(value)
                    }
                }
            }
            .toList()
            .awaitAll()
        results.forEach { (peer, result) ->
            result.onSuccess { addNode(peer) }
        }
        results
    }

    suspend fun findNodes(
        target: Key,
        peers: Iterable<DhtPeer> = getClosestLocalPeers(target, localNode.shortId).asIterable(),
    ): Flow<Pair<DhtPeer, Result<DhtNodes>>> {
        return query(
            target = target,
            query = { it.findNode(target.hash, K_VALUE) },
            newNodesExtractor = { _, n -> n },
            peers = peers,
        )
    }

    private suspend fun bootstrap(bucket: Int) {
        val distance = Distance.randomDistanceForBucket(bucket)
        val key = localKey.forDistance(distance)
        println("Bootstrapping DHT with distance $distance, key: ${key.hash.toHexString()}")
        query(
            target = localKey,
            query = { it.findNode(key.hash, K_VALUE) },
            newNodesExtractor = { _, nodes -> nodes },
            a = ALPHA_VALUE * 2
        ).collect { (peer, result) ->
            if (result.isSuccess) {
                println("success: ${peer.key}, ${result.getOrNull()}")
            } else {
                println("error: ${peer.key}, ${result.exceptionOrNull()?.message}")
            }
        }
        println("done, peers = ${routingTable.sumOf { it.size }}")
        getClosestLocalPeers(routingTable.localKey, localNode.shortId).forEach {
            val distance = it.key.distance(routingTable.localKey)
            println("Peer: ${it.key}, distance: ${distance.ilog2()} ${distance.value.toHexString()}")
        }
    }

    companion object {
        val BOOTSTRAP_NODES by lazy {
            listOf(
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("DA0H568bb+LoO2LGY80PgPee59jTPCqqSJJzt1SH+KE=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("172.105.29.108", 14583)),
                    signature = Base64.decodeToByteString("cL79gDTrixhaM9AlkCdZWccCts7ieQYQBmPxb/R7d7zHw3bEHL8Le96CFJoB1KHu8C85iDpFK8qlrGl1Yt/ZDg==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("Fhldu4zlnb20/TUj9TXElZkiEmbndIiE/DXrbGKu+0c=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("135.181.132.253", 6302)),
                    signature = Base64.decodeToByteString("nUGB77UAkd2+ZAL5PgInb3TvtuLLXJEJ2icjAUKLv4qIGB3c/O9k/v0NKwSzhsMP0ljeTGbcIoMDw24qf3goCg==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("GIpxz5qHuKu/kNZl7Zc/w8LbxYamzCBKpbT9+FAnIJU=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("89.39.107.33", 2810)),
                    signature = Base64.decodeToByteString("RvSTi5eaZ7wH1ap0EfffnT66CccrJA2JZtwb8tYOlxhaxtlRqwPOr0Om1pWBmbItwwAGScCxoYZFtdGbEImbDw==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("LFnKVKTO+GYsOBrTH2xaVAGsOGEgSNGo0TRdDZmBeL4=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("144.76.36.181", 21533)),
                    signature = Base64.decodeToByteString("UqP2Mvgtu4f/70NdsFekQ3jarIpcBQDamGn2jYhocw4yWTfHaxnP6m4FMh+qSe07q7e0DbkBjwKnGxu+YfIBCQ==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("MJr8xja0xpu9DoisFXBrkNHNx1XozR7HHw9fJdSyEdo=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("135.181.132.198", 6302)),
                    signature = Base64.decodeToByteString("XcR5JaWcf4QMdI8urLSc1zwv5+9nCuItSE1EDa0dSwYF15R/BtJoKU5YHA4/T8SiO18aVPQk2SL1pbhevuMrAQ==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("MKaJGWvZ8AOX/VWV2rJglcEJh07MKjgZiy5Mel6L2Xk=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("62.210.158.13", 7694)),
                    signature = Base64.decodeToByteString("GJiGFKV5sfOJuKNj13Bo7TfEk8A7NMyAruzj1nwWvfSlGSmnYUhUa9LmyHU7XyrlKcLmYC+MU0h5SctUkctsCA==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("aeMgdMdkkbkfAS4+n4BEGgtqhkf2/zXrVWWECOJ/h3A=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("167.172.48.179", 25975)),
                    signature = Base64.decodeToByteString("z5ogivZWpQchkS4UR4wB7i2pfOpMwX9Nd/USxinL9LvJPa+/Aw3F1AytR9FX0BqDftxIYvblBYAB5JyAmlj+AA==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("gzUNJnBJhdpooYCE8juKZo2y4tYDIQfoCvFm0yBr7y0=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("5.78.60.12", 54390)),
                    signature = Base64.decodeToByteString("LCrCkjmkMn6AZHW2I+oRm1gHK7CyBPfcb6LwsltskCPpNECyBl1GxZTX45n0xZtLgyBd/bOqMPBfawpQwWt1BA==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("jXiLaOQz1HPayilWgBWhV9xJhUIqfU95t+KFKQPIpXg=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("5.161.60.160", 12485)),
                    signature = Base64.decodeToByteString("fKSZh9nXMx+YblkQXn3I/bndTD0JZ1yAtK/tXPIGruNglpe9sWMXR+8fy3YogPhLJMdjNiMom1ya+tWG7qvBAQ==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("pdeuI0a/RhBqgkQxn+6+J2EMdcpTi0WhhaR8Q3/+u+4=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("131.153.232.195", 13654)),
                    signature = Base64.decodeToByteString("Ph5LdoX9NyBFDqF5YLS2jNdb/omco3pgmCt0iI99tS95Mcic+WFUsH9nt0zyzy1dd8D75vR952go2HMHpKYaBA==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("rNzhnAlmtRn9rTzW6o2568S6bbOXly7ddO1olDws5wM=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("128.199.52.250", 45943)),
                    signature = Base64.decodeToByteString("sn/+ZfkfCSw2bHnEnv04AXX/Goyw7+StHBPQOdPr+wvdbaJ761D7hyiMNdQGbuZv2Ep2cXJpiwylnZItrwdUDg==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("sbsuMcdyYFSRQ0sG86/n+ZQ5FX3zOWm1aCVuHwXdgs0=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("45.63.114.174", 50187)),
                    signature = Base64.decodeToByteString("9FJwbFw3IECRFkb9bA54YaexjDmlNBArimWkh+BvW88mjm3K2i5V2uaBPS3GubvXWOwdHLE2lzQBobgZRGMyCg==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("vhFPq+tgjJi+4ZbEOHBo4qjpqhBdSCzNZBdgXyj3NK8=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("5.22.218.95", 36752)),
                    signature = Base64.decodeToByteString("kBwAIgJVkz8AIOGoZcZcXWgNmWq8MSBWB2VhS8Pd+f9LLPIeeFxlDTtwAe8Kj7NkHDSDC+bPXLGQZvPv0+wHCg==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("4R0C/zU56k+x2HGMsLWjX2rP/SpoTPIHSSAmidGlsb8=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("139.162.201.65", 14395)),
                    signature = Base64.decodeToByteString("0uwWyCFn2KjPnnlbSFYXLZdwIakaSgI9WyRo87J3iCGwb5TvJSztgA224A9kNAXeutOrXMIPYv1b8Zt8ImsrCg==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("6PGkPQSbyFp12esf1NqmDOaLoFA8i9+Mp5+cAx5wtTU=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("185.86.79.9", 22096)),
                    signature = Base64.decodeToByteString("L4N1+dzXLlkmT5iPnvsmsixzXU0L6kPKApqMdcrGP5d9ssMhn69SzHFK+yIzvG6zQ9oRb4TnqPBaKShjjj2OBg==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("+htkM588jJXXidOs64fuGj/jiZiCY/AG3EljcugfOs0=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("131.153.243.173", 17750)),
                    signature = Base64.decodeToByteString("K9zywmobUDVsYlXdJwTg1b9xtbSNueX6cizpI26xD71ntQbAURyd6TLXUzEezdYZYSzTvK0NJoL2VojqBgFBAw==")
                ),
                DhtNode(
                    id = PublicKeyEd25519(Base64.decode("/YDNd+IwRUgL0mq21oC0L3RxrS8gTu0nciSPUrhqR78=")),
                    addressList = AdnlAddressList(AdnlAddress.Udp("172.104.59.125", 14432)),
                    signature = Base64.decodeToByteString("6+oVk6HDtIFbwYi9khCc8B+fTFceBUo1PWZDVTkb4l84tscvr5QpzAkdK7sS5xGzxM7V7YYQ6gUQPrsP9xcLAw==")
                ),
            )
        }
    }
}

class DhtPeer(
    val dht: DhtLocalNode,
    val peerPair: AdnlPeerPair,
    initialInfo: DhtNode
) : DhtService {
    val key = DhtKeyId(peerPair.remoteNode.shortId)
    var info: DhtNode = initialInfo

    override suspend fun <T> query(query: DhtFunction<T>, serializer: KSerializer<DhtFunction<T>>): T {
        val rawQuery = TL.Boxed.encodeToByteArray(serializer, query)
        val rawAnswer = peerPair.query(rawQuery)
        return try {
            TL.Boxed.decodeFromByteArray(query.answerSerializer, rawAnswer)
        } catch (e: Exception) {
            throw RuntimeException("Failed to decode DHT response for query: $query [${rawAnswer.toHexString()}]", e)
        }
    }

    suspend fun checkConnection(): Boolean {
        var delayTime = 500L
        repeat(3) {
            runCatching {
                withTimeout(delayTime) {
                    ping(Random.nextLong())
                }
            }.onFailure {
                delayTime = (delayTime * 2).coerceAtMost(5000L)
            }.onSuccess {
                return true
            }
        }
        return false
    }

    override suspend fun ping(randomId: Long): DhtPong {
        return query(DhtFunction.Ping(randomId), DhtFunction.serializer(DhtPong.serializer()))
    }

    override suspend fun store(value: DhtValue): DhtStored {
        return query(DhtFunction.Store(value), DhtFunction.serializer(DhtStored.serializer()))
    }

    override suspend fun findNode(key: ByteString, k: Int): DhtNodes {
        return query(DhtFunction.FindNode(key, k), DhtFunction.serializer(DhtNodes.serializer()))
    }

    override suspend fun findValue(
        key: ByteString,
        k: Int
    ): DhtValueResult {
        return query(DhtFunction.FindValue(key, k), DhtFunction.serializer(DhtValueResult.serializer()))
    }

    override suspend fun getSignedAddressList(): DhtNode {
        return query(DhtFunction.GetSignedAddressList, DhtFunction.serializer(DhtNode.serializer()))
    }

    override suspend fun registerReverseConnection(
        node: AdnlIdFull,
        ttl: Int,
        signature: ByteString
    ): DhtStored {
        return query(
            DhtFunction.RegisterReverseConnection(node, ttl, signature),
            DhtFunction.serializer(DhtStored.serializer())
        )
    }

    override suspend fun requestReversePing(
        target: AdnlNode,
        signature: ByteString,
        client: AdnlIdShort,
        k: Int
    ): DhtReversePingResult {
        return query(
            DhtFunction.RequestReversePing(target, signature, client, k),
            DhtFunction.serializer(DhtReversePingResult.serializer())
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DhtPeer

        return key == other.key && peerPair.localNode.id == other.peerPair.localNode.id
    }

    override fun hashCode(): Int {
        return key.hashCode() xor peerPair.localNode.id.hashCode()
    }
}

data class DhtConfig(
    val kBucketConfig: KBucketConfig = KBucketConfig(),

    /**
     * The replication factor determines to how many closest peers
     * a [DhtValue] is replicated. The default is [K_VALUE].
     */
    val replicationFactor: Int = K_VALUE,
)
