@file:Suppress("OPT_IN_USAGE")

package org.ton.proxy.dht

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.dht.DhtNode
import org.ton.kotlin.adnl.dht.DhtValueFound
import org.ton.kotlin.adnl.dht.DhtValueNotFound
import org.ton.kotlin.adnl.dht.DhtValueResult
import org.ton.kotlin.adnl.dht.functions.DhtFindValue
import org.ton.logger.Logger
import org.ton.logger.PrintLnLogger

class DhtValuesFlow(
    val key: ByteArray,
    knownPeers: Collection<DhtPeer>
) : AbstractFlow<Pair<DhtPeer, DhtValueResult?>>() {
    private val query = DhtFindValue(key, 6).toByteArray()
    private val knownPeers = HashSet<AdnlIdShort>()
    private val peersChannel = Channel<DhtPeer>(
        capacity = Channel.UNLIMITED
    )
    private val logger = PrintLnLogger("DhtValuesFlow", Logger.Level.DEBUG)

    init {
        knownPeers.forEachIndexed { index, dhtPeer ->
            if (index < MAX_BATCH_COUNT) {
                addNode(dhtPeer.dhtNode)
            } else {
                return@forEachIndexed
            }
        }
    }

    companion object {
        const val QUERY_TIMEOUT = 1_000L
        const val MAX_BATCH_COUNT = 10
    }

    @FlowPreview
    override suspend fun collectSafely(collector: FlowCollector<Pair<DhtPeer, DhtValueResult?>>): Unit =
        coroutineScope {
            while (isActive && !peersChannel.isEmpty) {
                var batchCount = 0
                val results = ArrayList<Pair<DhtPeer, Deferred<DhtValueResult?>>>(MAX_BATCH_COUNT)
                while (batchCount++ < MAX_BATCH_COUNT && !peersChannel.isEmpty) {
                    val peer = peersChannel.receive()
                    val result = peer to async {
                        withTimeoutOrNull(QUERY_TIMEOUT) {
                            DhtValueResult.decodeBoxed(peer.rawQuery(query))
                        }
                    }
                    results.add(result)
                }
                val nodes = ArrayList<DhtNode>()
                for ((peer, asyncValue) in results) {
                    val value = asyncValue.await()
                    logger.debug { "receive value: ${value?.let { it::class.simpleName }} - $peer" }
//                    println("found in: ${Dht.affinity(key, peer.dhtNode.id.toAdnlIdShort().id)}")
                    collector.emit(peer to value)
                    if (value is DhtValueFound) {
                        return@coroutineScope
                    } else if (value is DhtValueNotFound) {
                        nodes.addAll(value.nodes)
                    }
                }
                val affinityNodes = nodes.map {
                    val nodeId = it.id.toAdnlIdShort().id
                    val affinity = Dht.affinity(key, nodeId)
                    it to affinity
                }.sortedByDescending { it.second }
                var added = 0
                val iterator = affinityNodes.iterator()
                while (added < MAX_BATCH_COUNT && iterator.hasNext()) {
                    val (node, affinity) = iterator.next()
                    if (addNode(node)) {
                        added++
                    }
                }
            }
        }


    private fun addNode(node: DhtNode): Boolean {
        val id = node.id.toAdnlIdShort()
        return if (knownPeers.add(id)) {
            logger.debug { "New peer added: $id" }
            DhtPeer(node)?.let {
                peersChannel.trySend(it)
            }
            true
        } else {
            logger.debug { "Already known: $node" }
            false
        }
    }
}
