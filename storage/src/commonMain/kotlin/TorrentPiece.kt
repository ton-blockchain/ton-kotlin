package org.ton.kotlin.storage

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.adnl.util.Hash256Map
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

private val logger = KtorSimpleLogger("org.ton.kotlin.storage.TorrentPiece")

class TorrentPiece(
    val torrent: Torrent,
    val id: Int,
    var priority: Int,
) : CoroutineScope, Comparable<TorrentPiece> {
    override val coroutineContext: CoroutineContext = torrent.coroutineContext + CoroutineName("piece #$id")
    private val peers = MutableStateFlow<Map<AdnlIdShort, TorrentPeer>>(emptyMap())

    override fun compareTo(other: TorrentPiece): Int {
        return compareValuesBy(
            this, other,
            { -it.priority },
            { it.peers.value.size },
            { it.id }
        )
    }

    suspend fun download(): StoragePiece = coroutineScope {
        var piece: StoragePiece? = null
        logger.debug("start download piece #$id")
        var peers = peers.first { it.isNotEmpty() }.values
        while (piece == null) {
            peers = peers.shuffled()
            logger.debug("piece #$id got ${peers.size} peers")
            for (peer in peers) {
                logger.debug("piece #$id start download from peer ${peer.debugStr()}")
                piece = withTimeoutOrNull(15.seconds) {
                    peer.getPiece(id)
                }
                if (piece != null) {
                    break
                } else {
                    logger.debug("piece #$id failed to download from peer ${peer.debugStr()}")
                }
            }
            peers = this@TorrentPiece.peers.value.values
        }
        piece
    }

    fun addPeer(peer: TorrentPeer) {
        if (peers.value[peer.id] == peer) return
        peers.update { oldMap ->
            val newMap = Hash256Map<AdnlIdShort, TorrentPeer>({ it.hash })
            newMap.putAll(oldMap)
            newMap[peer.id] = peer
            newMap
        }
    }

    fun removePeer(peer: TorrentPeer) {
        if (!peers.value.contains(peer.id)) return
        peers.update { oldMap ->
            oldMap - peer.id
        }
    }
}
