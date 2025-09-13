package org.ton.kotlin.overlay

import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.adnl.AdnlPeerPair
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.dht.*
import org.ton.kotlin.overlay.broadcast.BroadcastInfo
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random
import kotlin.time.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val LOGGER = KtorSimpleLogger("org.ton.kotlin.overlay.Overlay")

class Overlay internal constructor(
    val localNode: OverlayLocalNode,
    val id: OverlayIdFull,
    val type: OverlayType,
    val options: OverlayOptions = OverlayOptions(),
) : CoroutineScope {
    private val dhtKey = DhtKey(id.shortId.publicKeyHash, "nodes")
    private val dhtKeyDescription = DhtKeyDescription(dhtKey, id.publicKey, DhtUpdateRule.OverlayNodes)
    private val coroutineName =
        CoroutineName("overlay.${id.name.toHexString().take(8)}@${localNode.shortId.toString().take(8)}")

    override val coroutineContext: CoroutineContext =
        localNode.coroutineContext + coroutineName

    private val _incomingMessages = Channel<ByteString>(CONFLATED)
    val incomingMessages: ReceiveChannel<ByteString> = _incomingMessages

    private val _incomingQueries = Channel<ByteString>(CONFLATED)
    val incomingQueries: ReceiveChannel<ByteString> = _incomingQueries

    private val _incomingBroadcasts = Channel<ByteString>(CONFLATED)
    val incomingBroadcasts: ReceiveChannel<ByteString> = _incomingBroadcasts

    val reconnectBackoff: Duration get() = options.reconnectBackoff
    val pingInterval: Duration get() = options.pingInterval

    private val knownPeers = Hash256Map<AdnlIdShort, OverlayNodeInfo>({ it.hash })
    private val activeNeighbors = Hash256Map<AdnlIdShort, OverlayNeighbour>({ it.hash })

    private val dhtQueryJob = launch(CoroutineName("dhtQuery")) {
        LOGGER.debug { "started dht query job" }
        while (isActive) {
            runCatching {
                runDhtQuery()
            }.onFailure {
                delay(1000)
            }.onSuccess {
                delay(Random.nextLong(60_000, 120_000))
            }
        }
    }
    private val randomNeighbour = launch(CoroutineName("trimAndRebalance")) {
        while (isActive) {
            delay(30.seconds)
            trimAndRebalance()

            val (fast, mid, slow) = classifyByRtt(activeNeighbors.values)

            (fast + mid + slow).randomTake(3).map {
                launch {
                    it.getRandomPeer()?.forEach { randomPeerInfo ->
                        activeNeighbors.getOrPut(randomPeerInfo.id.shortId) {
                            OverlayNeighbour(randomPeerInfo, false)
                        }
                    }
                }
            }.joinAll()
        }
    }

    private val peersLogJob = launch(CoroutineName("activePeersLog")) {
        while (isActive) {

            val (fast, mid, slow) = classifyByRtt(activeNeighbors.values)

            LOGGER.debug("known peers: ${knownPeers.size}, active peers: ${activeNeighbors.size}")
            fast.forEach {
                LOGGER.debug("fast ${it.info.id.shortId} ${it.rtt}")
            }
            mid.forEach {
                LOGGER.debug("mid ${it.info.id.shortId} ${it.rtt}")
            }
            slow.forEach {
                LOGGER.debug("slow ${it.info.id.shortId} ${it.rtt}")
            }
            LOGGER.debug("=============")

            delay(1000)
        }
    }

    private val processQueryJob = launch(CoroutineName("OverlayProcessQueries")) {
        localNode.queries.collect {
            if (it.overlay == this@Overlay) {
                onQuery(it)
            }
        }
    }

    private val _newPeers =
        MutableSharedFlow<OverlayNodeInfo>(replay = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val newPeers: SharedFlow<OverlayNodeInfo> get() = _newPeers

    val neighbours: List<AdnlPeerPair>
        get() {
            val (fast, mid, slow) = classifyByRtt(activeNeighbors.values)
            return buildList {
                fast.forEach {
                    it.peerPairOrNull()?.let(::add)
                }
                mid.forEach {
                    it.peerPairOrNull()?.let(::add)
                }
                slow.forEach {
                    it.peerPairOrNull()?.let(::add)
                }
            }
        }


    private fun classifyByRtt(peers: Collection<OverlayNeighbour>): Triple<List<OverlayNeighbour>, List<OverlayNeighbour>, List<OverlayNeighbour>> {
        val kFast = options.targetFastNeighbours.coerceAtLeast(0)
        val kMid = options.targetMidNeighbours.coerceAtLeast(0)
        val kSlow = options.targetSlowNeighbours.coerceAtLeast(0)

        val measured = peers.filter { it.rtt.isFinite() }
        if (measured.isEmpty()) return Triple(emptyList(), emptyList(), emptyList())

        val fast = measured.sortedBy { it.rtt }.take(kFast)
        val fastIds = fast.map { it.shortId }.toHashSet()

        val rest = measured.filter { it.shortId !in fastIds }
        val slow = rest.randomTake(kSlow)
        val slowIds = slow.map { it.shortId }.toHashSet()

        val rFastMax = fast.maxOf { it.rtt }
        val rSlowMin = slow.minOfOrNull { it.rtt }
        val pool = rest.filter { it.shortId !in slowIds }
            .filter { it.rtt > rFastMax }
            .sortedBy { it.rtt }

        // Стартовая верхняя граница без прибавления константы
        var upper = rSlowMin
            ?.takeIf { it > rFastMax }
            ?.let { (rFastMax + it) / 2 }
            ?: (rFastMax * 1.2) // fallback: просто на 20% больше fastMax

        var mid = pool.takeWhile { it.rtt <= upper }.take(kMid)

        // Расширяем коридор, если mid не добран
        if (mid.size < kMid && pool.isNotEmpty()) {
            val p90 = pool[(pool.lastIndex * 9) / 10].rtt
            while (mid.size < kMid && upper < p90) {
                upper *= 1.25
                mid = pool.takeWhile { it.rtt <= upper }.take(kMid)
            }
        }

        if (mid.size < kMid) {
            val need = kMid - mid.size
            val taken = mid.map { it.shortId }.toHashSet()
            mid = mid + pool.filter { it.shortId !in taken }.take(need)
        }

        return Triple(fast, mid, slow)
    }

    private fun trimAndRebalance() {
        LOGGER.debug("start rebalancing: ${activeNeighbors.size}")

        val all = activeNeighbors.values
        val (fast, mid, slow) = classifyByRtt(all)
        val classified = fast + mid + slow
        val deficitFast = options.targetFastNeighbours - fast.size
        val deficitMid = options.targetMidNeighbours - mid.size
        val deficitSlow = options.targetSlowNeighbours - slow.size
        val deficit = (deficitFast + deficitSlow + deficitMid).coerceAtLeast(0)
        if (deficit > 0) {
            val peers = all.associateBy { it.info.id.shortId }.keys
            knownPeers.values.asSequence()
                .filter { it.id.shortId !in peers }
                .shuffled()
                .take(deficit)
                .forEach {
                    activeNeighbors[it.id.shortId] = OverlayNeighbour(it, false)
                }
        }

        if (activeNeighbors.size > options.maxNeighbours) {
            val toDrop = activeNeighbors.values
                .filter { it !in classified }
                .sortedWith(compareByDescending<OverlayNeighbour> { it.isIdle }.thenByDescending { it.rtt })
                .take(activeNeighbors.size - options.maxNeighbours)
            toDrop.forEach { p ->
                activeNeighbors.remove(p.info.id.shortId)?.cancel()
            }
        }

        LOGGER.debug("rebalancing survivors: ${activeNeighbors.size}")
//        LOGGER.info("known peers: ${knownPeers.size}, active peers: ${activePeers.size}")
//        fast.forEach {
//            LOGGER.info("fast ${it.info.id.shortId} ${it.rtt}")
//        }
//        mid.forEach {
//            LOGGER.info("mid ${it.info.id.shortId} ${it.rtt}")
//        }
//        slow.forEach {
//            LOGGER.info("slow ${it.info.id.shortId} ${it.rtt}")
//        }
//        LOGGER.info("=============")
    }

    private suspend fun runDhtQuery() {
        LOGGER.debug { "start DHT query" }
        val nodes = HashSet<AdnlIdShort>()
        val dhtUpdateCandidates = ArrayList<DhtPeer>()
        val peerCandidate = Channel<OverlayNeighbour>(Channel.UNLIMITED)
        val collectAll = launch {
            var peer: OverlayNeighbour? = peerCandidate.receive()
            while (isActive) {
                if (peer == null) {
                    peer = peerCandidate.tryReceive().getOrNull() ?: break
                }
//                LOGGER.info("got peer: ${peer.shortId}")
                peer.getRandomPeer()?.forEach { nodeInfo ->
                    if (addPeer(nodeInfo) && nodes.add(nodeInfo.id.shortId)) {
                        val peer = activeNeighbors.getOrPut(nodeInfo.id.shortId) {
                            OverlayNeighbour(nodeInfo, true)
                        }
                        peerCandidate.send(peer)
                    }
                }
                peer = null
            }
        }
        localNode.overlayNodeProvider.getNodes(id.shortId)
            .collect {
                it.forEach { nodeInfo ->
                    val id = nodeInfo.id.shortId
                    if (addPeer(nodeInfo) && nodes.add(id)) {
                        val peer = activeNeighbors.getOrPut(id) {
                            OverlayNeighbour(nodeInfo, true)
                        }
                        peerCandidate.send(peer)
                    }
                }
            }

        LOGGER.info("found ${nodes.size} nodes from DHT")
        collectAll.join()
        LOGGER.info("done collecting all")
//        dhtUpdateCandidates.sortBy { it.key.distance(dhtKey) }
//        LOGGER.info(
//            "sorted dht update candidates: ${dhtUpdateCandidates.size} \n${
//                dhtUpdateCandidates.joinToString("\n") {
//                    "${it.key} - ${
//                        it.key.distance(
//                            dhtKey
//                        )
//                    }"
//                }
//            }"
//        )

        if (options.announceSelf) {
            val selfNodeInfo = getSelfNodeInfo()
            updateDhtNodes(selfNodeInfo, dhtUpdateCandidates)
        } else {

        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun updateDhtNodes(node: OverlayNodeInfo, candidates: List<DhtPeer> = emptyList()) {
        val nodes = OverlayNodeInfoList(node)
        val dhtValue = DhtValue(
            dhtKeyDescription,
            TL.Boxed.encodeToByteString(nodes),
            (Clock.System.now() + 1.hours).epochSeconds.toInt()
        )
        val storedPeers = localNode.dht.storeValue(dhtValue)
        if (candidates.isNotEmpty()) {
            val key2peers = storedPeers.mapTo(HashSet()) { (peer, _) -> peer.key }
            val correctionCandidates = candidates.filter {
                it.key !in key2peers
            }
            localNode.dht.storeValue(dhtValue, correctionCandidates)
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun getSelfNodeInfo(version: Int = Clock.System.now().epochSeconds.toInt()): OverlayNodeInfo {
        return OverlayNodeInfo(localNode.id, id.shortId, version).signed(localNode.key)
    }

    private fun addPeer(node: OverlayNodeInfo): Boolean {
        if (node.overlay != id.shortId) return false
        if (node.id.shortId == localNode.shortId) return false
        val key = node.id.shortId
        val oldInfo = knownPeers[key]
        if (!knownPeers.contains(key)) {
//            LOGGER.info("[$this] found node: $node")
        }
        if (oldInfo == null || oldInfo.version < node.version) {
            knownPeers[key] = node
            activeNeighbors[key]?.info = node
            _newPeers.tryEmit(node)
            return true
        }
        return false
    }

    private suspend fun onQuery(query: OverlayQuery) {
        val queryFunction = runCatching {
            TL.Boxed.decodeFromByteString<OverlayFunction>(query.input)
        }.getOrNull()
        when (queryFunction) {
            is OverlayFunction.GetBroadcast -> {
                query.respond(TL.Boxed.encodeToByteString(BroadcastInfo.NotFound))
            }

            is OverlayFunction.GetRandomPeers -> {
                queryFunction.peers.forEach {
                    addPeer(it)
                }
                val neighbours = activeNeighbors.values.filter { neighbour ->
                    val neighbourId = neighbour.info.id.shortId
                    neighbourId != query.peer.remoteNode.id.shortId && queryFunction.peers.nodes.none {
                        it.id.shortId == neighbourId || it.version >= neighbour.info.version
                    }
                }
                val ourRandomPeers = classifyByRtt(neighbours)
                    .let { (fast, mid, slow) -> fast + mid + slow }
                    .shuffled()
                    .take(3)
                    .map { it.info }
                val answer = OverlayNodeInfoList(ourRandomPeers)
                query.respond(TL.Boxed.encodeToByteString(answer))
            }

            is OverlayFunction.Query -> {
            }

            OverlayFunction.Ping -> {
                query.respond(TL.Boxed.encodeToByteString(OverlayFunction.Pong))
            }

            else -> {
            }
        }
    }

    override fun toString(): String = coroutineName.name

    @OptIn(ExperimentalTime::class)
    private inner class OverlayNeighbour(
        var info: OverlayNodeInfo,
        instantInit: Boolean
    ) : CoroutineScope {
        val shortId get() = info.id.shortId

        var rtt: Duration = Duration.INFINITE
            private set
        var lastActiveAt: Instant = Instant.DISTANT_PAST
            private set
        var isIdle = false
            private set

        private val job = Job()
        override val coroutineContext: CoroutineContext = this@Overlay.coroutineContext + job + CoroutineName("peer")
        private var adnlPeerPair: AdnlPeerPair? = null
        private var nextRetryAt: Instant = Instant.DISTANT_PAST
        private var consecFails = 0
        private var successCount = 0
        private var windowTimeouts = 0
        private var windowAttempts = 0
        private val connectionMutex = Mutex()

        private val pingJob = launch {
            if (!instantInit) {
                val initialJitter = Random.nextLong(1000)
                delay(initialJitter)
            }

            while (isActive) {
                try {
                    val connection = ensureConnection()
                    if (connection == null) {
                        windowAttempts++
                        windowTimeouts++
                        delay(nextRetryAt - Clock.System.now())
                        continue
                    }
                    windowAttempts++
                    val randomPeers = withTimeout(options.requestTimeout) {
                        val ourRandomPeers = classifyByRtt(activeNeighbors.values)
                            .let { (fast, mid, slow) -> fast + mid + slow }
                            .filter { it.info.id.shortId != info.id.shortId }
                            .shuffled()
                            .take(3)
                            .map { it.info }
                        connection.getRandomPeers(ourRandomPeers)
                    }
                    successCount++
//                    LOGGER.info("Pinged peer ($rtt) ${info.id.shortId}")
                    lastActiveAt = Clock.System.now()
                    isIdle = false
                    randomPeers.forEach {
                        addPeer(it)
                    }
                    val jitter = Random.nextDuration(pingInterval / 5)
                    delay((pingInterval + jitter).coerceAtLeast(1.seconds))
                } catch (e: TimeoutCancellationException) {
                    windowTimeouts++
                    isIdle = true
                    delay(reconnectBackoff)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    windowTimeouts++
                    isIdle = true
                    adnlPeerPair = null
                    delay(reconnectBackoff)
                }
            }
        }.invokeOnCompletion {
            LOGGER.info("peer ${info.id.shortId} rtt:$rtt isIdle:$isIdle destroyed: $it")
            activeNeighbors.remove(info.id.shortId)
        }

        fun peerPairOrNull() = adnlPeerPair

        private suspend fun ensureConnection(): AdnlPeerPair? {
            adnlPeerPair?.let { return it }

            val now = Clock.System.now()
            if (now < nextRetryAt) return null

            val adnlNode = withTimeoutOrNull(15.seconds) {
                localNode.adnlNodeResolver.resolveAdnlNode(info.id)
            }
            if (adnlNode == null) {
                consecFails++
                val base = reconnectBackoff
                val exp = (base * (1 shl consecFails.coerceAtMost(6))).coerceAtMost(2.minutes)
                val jitter = Random.nextLong(0, (base / 2).inWholeMilliseconds).milliseconds
                val delay = exp + jitter
                LOGGER.info("failed to connect, delay: $delay ${info.id.shortId} ")
                nextRetryAt = now + delay
                isIdle = true
                return null
            } else {
                consecFails = 0
                nextRetryAt = Instant.DISTANT_PAST
                isIdle = false

                return connectionMutex.withLock {
                    localNode.adnl.peer(adnlNode).also {
                        adnlPeerPair = it
                    }
                }
            }
        }

        suspend fun AdnlPeerPair.rawQuery(rawQuery: ByteArray): ByteString {
            val queryPrefix = TL.Boxed.encodeToByteArray(OverlayFunction.Query(id))
            val (rawAnswer, sample) = measureTimedValue {
                query(queryPrefix + rawQuery)
            }
            rtt = ewma(rtt, sample, 0.2)
            return ByteString(*rawAnswer)
        }

        private fun ewma(prev: Duration, sample: Duration, alpha: Double) =
            if (prev.isInfinite()) sample else alpha * sample + (1 - alpha) * prev

        suspend fun getRandomPeer(): OverlayNodeInfoList? {
            val connection = ensureConnection() ?: return null
            return withTimeoutOrNull(options.requestTimeout) {
                connection.getRandomPeers()
            }
        }

        suspend fun AdnlPeerPair.getRandomPeers(list: List<OverlayNodeInfo> = emptyList()): OverlayNodeInfoList {
            val query = TL.Boxed.encodeToByteArray(OverlayFunction.GetRandomPeers(OverlayNodeInfoList(list)))
            return TL.Boxed.decodeFromByteString<OverlayNodeInfoList>(
                rawQuery(query)
            )
        }
    }
}

private fun Random.nextDuration(until: Duration) = nextLong(until.inWholeMilliseconds).milliseconds

private fun percentile(sorted: List<Double>, p: Double): Double {
    if (sorted.isEmpty()) return Double.NaN
    if (sorted.size == 1) return sorted[0]
    val clampedP = p.coerceIn(0.0, 100.0)
    val rank = (clampedP / 100.0) * (sorted.size - 1)
    val lo = rank.toInt()
    val hi = (lo + 1).coerceAtMost(sorted.size - 1)
    val frac = rank - lo
    return sorted[lo] * (1 - frac) + sorted[hi] * frac
}

private fun <T> List<T>.randomTake(k: Int) =
    if (k <= 0 || isEmpty()) emptyList() else if (size <= k) this else shuffled().take(k)
