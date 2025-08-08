package org.ton.kotlin.adnl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.io.Buffer
import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.PrivateKeyEd25519
import kotlin.coroutines.CoroutineContext

class AdnlLocalNode internal constructor(
    val key: PrivateKeyEd25519,
    val adnl: Adnl
) : CoroutineScope {
    val id = AdnlIdFull(key.publicKey())
    val shortId get() = id.idShort

    private val peersMap = Hash256Map<AdnlPeerPair>()
    private val channelCache = Hash256Map<AdnlChannel>()
    val peers: Collection<AdnlPeerPair> = peersMap.values
    override val coroutineContext: CoroutineContext = adnl.coroutineContext + SupervisorJob()

    suspend fun sendDatagram(datagram: Buffer, address: AdnlAddress) {
        adnl.sendDatagram(datagram, address)
    }

    fun channel(id: AdnlIdShort): AdnlChannel? {
        var channelCandidate = channelCache[id.hash]
        if (channelCandidate != null) {
            if (peersMap.containsKey(channelCandidate.peerPair.remoteId.idShort.hash)) {
                return channelCandidate
            } else {
                channelCache.remove(id.hash)
            }
        }
        for (peer in peers) {
            channelCandidate = peer.channel
            if (channelCandidate != null && channelCandidate.inId == id) {
                channelCache[id.hash] = channelCandidate
                return channelCandidate
            }
        }
        return null
    }

    fun peer(id: AdnlIdShort): AdnlPeerPair? = peersMap[id.hash]

    fun peer(
        id: AdnlIdFull,
        initialAddress: AdnlAddress
    ): AdnlPeerPair {
        return peersMap.getOrPut(id.idShort.hash) {
            AdnlPeerPair(this, id, initialAddress)
        }
    }

    suspend fun processQuery(pair: AdnlPeerPair, query: Buffer): Buffer? {

        return null
    }
}
