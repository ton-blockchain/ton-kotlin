package org.ton.kotlin.adnl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.io.Buffer
import kotlinx.io.IOException
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.PrivateKeyEd25519
import kotlin.coroutines.CoroutineContext

class AdnlLocalNode internal constructor(
    val key: PrivateKeyEd25519,
    val adnl: Adnl
) : CoroutineScope, AdnlNodeResolver {
    val id = AdnlIdFull(key.publicKey())
    val shortId get() = id.shortId

    private val peersMap = Hash256Map<AdnlIdShort, AdnlPeerPair> { it.hash }
    private val channelCache = Hash256Map<AdnlIdShort, AdnlChannel> { it.hash }
    val peers: Collection<AdnlPeerPair> = peersMap.values
    override val coroutineContext: CoroutineContext = adnl.coroutineContext + SupervisorJob()
    private val _newPeers = MutableSharedFlow<AdnlPeerPair>()
    val newPeers = _newPeers.asSharedFlow()

    private val queries_ = MutableSharedFlow<AdnlQuery>(extraBufferCapacity = 8)
    val queries = queries_.asSharedFlow()

    suspend fun sendDatagram(datagram: Buffer, address: AdnlAddress) {
        adnl.sendDatagram(datagram, address)
    }

    fun channel(id: AdnlIdShort): AdnlChannel? {
        var channelCandidate = channelCache[id]
        if (channelCandidate != null) {
            if (peersMap.containsKey(channelCandidate.peerPair.remoteNode.shortId)) {
                return channelCandidate
            } else {
                channelCache.remove(id)
            }
        }
        for (peer in peers) {
            channelCandidate = peer.channel
            if (channelCandidate != null && channelCandidate.inId == id) {
                channelCache[id] = channelCandidate
                return channelCandidate
            }
        }
        return null
    }

    fun peer(id: AdnlIdShort): AdnlPeerPair? = peersMap[id]

    fun peer(
        node: AdnlNode
    ): AdnlPeerPair {
        return peersMap.getOrPut(node.shortId) {
            val peer = AdnlPeerPair(this, node, queries_)
            _newPeers.tryEmit(peer)
            peer
        }
    }

    suspend fun dial(id: AdnlIdShort, resolver: AdnlNodeResolver = AdnlNodeResolver { null }): AdnlPeerPair {
        peer(id)?.let { return it }
        val adnlNode = resolver.resolveAdnlNode(id) ?: throw IOException("Can't resolve ADNL node: $id")
        return peer(adnlNode)
    }

    override suspend fun resolveAdnlNode(adnlIdShort: AdnlIdShort): AdnlNode? {
        return peersMap[adnlIdShort]?.remoteNode
    }
}
