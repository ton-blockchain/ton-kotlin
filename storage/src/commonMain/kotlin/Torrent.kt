package org.ton.kotlin.storage

import io.ktor.util.logging.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.io.write
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.adnl.AdnlNodeResolver
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.overlay.OverlayIdFull
import org.ton.kotlin.overlay.OverlayLocalNode
import org.ton.kotlin.overlay.OverlayQuery
import org.ton.kotlin.rldp.RldpConnection
import org.ton.kotlin.rldp.RldpLocalNode
import org.ton.kotlin.tl.TL
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val logger = KtorSimpleLogger("org.ton.kotlin.storage.Torrent")

@OptIn(ExperimentalTime::class)
class Torrent(
    val hash: ByteString,
    val rldp: RldpLocalNode,
    val overlays: OverlayLocalNode,
    val adnlNodeResolver: AdnlNodeResolver = overlays.adnlNodeResolver,
    coroutineContext: CoroutineContext = EmptyCoroutineContext
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        coroutineContext + CoroutineName("torrent.${hash.toHexString().take(8)}")

    val overlay = overlays.createOverlay(OverlayIdFull(hash))

    val overlayQueryJob = launch {
        overlays.queries.collect { query ->
            onQuery(query)
        }
    }

    @OptIn(ExperimentalTime::class)
    val sessionId = Clock.System.now().epochSeconds

    internal val torrentInfo = CompletableDeferred<TorrentInfo>(coroutineContext[Job])
    private val peers = Hash256Map<AdnlIdShort, TorrentPeer>({ it.hash })

    val piecesMask = async {
        val torrentInfo = torrentInfo.await()
        val mask = PiecesMask(torrentInfo.piecesCount)
        MutableStateFlow(mask)
    }

    val pieces = async {
        val torrentInfo = torrentInfo.await()
        List(torrentInfo.piecesCount) { index ->
            val priority = if (index < torrentInfo.headerPiecesCount) {
                Int.MAX_VALUE
            } else {
                0
            }
            TorrentPiece(this@Torrent, index, priority)
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    val storageState = MutableStateFlow(StorageState(false, true))

//    val pieces = async {
//        val torrentInfo = torrentInfo.await()
//        logger.debug { "pieces: total ${torrentInfo.piecesCount}" }
//        val piecesList = List(torrentInfo.piecesCount) { pieceId ->
//            this@Torrent.async(start = CoroutineStart.LAZY) {
//                logger.debug { "piece #$pieceId: start job" }
//                var piece: StoragePiece? = null
//                while (piece == null) {
//                    val piecePeers = Hash256Map<AdnlIdShort, TorrentPeer>({ it.hash })
//                    while (isActive) {
//                        logger.debug { "piece #$pieceId: start collecting peers" }
//                        for ((peerId, peer) in peers) {
//                            if (peer.piecesMask.isCompleted) {
//                                val peerPiecesMask = peer.piecesMask.await()
//                                if (peerPiecesMask.value.get(pieceId)) {
//                                    piecePeers[peerId] = peer
//                                }
//                            }
//                        }
//                        if (piecePeers.isEmpty()) {
//                            logger.debug { "piece #$pieceId: no peers found..." }
//                            delay(1000)
//                            continue
//                        }
//                        break
//                    }
//                    logger.debug { "piece #$pieceId: collected peers: ${piecePeers.size}" }
//                    val shuffledPeers = piecePeers.values.shuffled()
//                    piece = shuffledPeers.firstNotNullOfOrNull { peer ->
//                        withTimeoutOrNull(30.seconds) {
//                            logger.debug { "piece #$pieceId: start downloading from : ${peer.id}" }
//                            peer.getPiece(pieceId)
//                        }
//                    }
//                }
//                logger.debug { "piece #$pieceId: downloaded piece" }
//                piece
//            }
//        }
//        logger.debug { "pieces: got ${piecesList.size} pieces list" }
//        piecesList
//    }


    val header = async(start = CoroutineStart.LAZY) {
        val torrentInfo = torrentInfo.await()

        val pieces = pieces.await()
            .take(torrentInfo.headerPiecesCount)
            .map { async { it.download() } }
            .awaitAll()

        val buf = Buffer()
        var remainingBytes = torrentInfo.headerSize
        for (piece in pieces) {
            if (remainingBytes >= piece.data.size) {
                buf.write(piece.data)
                remainingBytes -= piece.data.size
            } else {
                buf.write(piece.data, 0, remainingBytes.toInt())
                remainingBytes = 0
            }
            if (remainingBytes <= 0L) {
                break
            }
        }
        TL.Boxed.decodeFromByteArray<TorrentHeader>(buf.readBytes())
    }

    private val newPeersJob = launch {
        overlay.newPeers.collect {
            println("new torrent peer: ${it.id.shortId} | ")
            val torrentPeer = peers.getOrPut(it.id.shortId) {
                TorrentPeer(this@Torrent, it.id.shortId)
            }
//            if (!torrentInfoDeferred.isCompleted) {
//                torrentPeer.launch {
//                    val torrentInfo = torrentPeer.getTorrentInfo()
//                    val boc = BagOfCells(torrentInfo.data)
//                    val cell = boc.loadCell(boc.getRootCell())
//                    val info = Buffer().run {
//                        write(cell.bits.toByteArray())
//                        val pieceSize = readInt()
//                        val totalSize = readLong()
//                        val rootHash = readByteString(32)
//                        val headerSize = readLong()
//                        val headerHash = readByteString(32)
//                        val d = ByteWrappingBitString(readByteArray())
//                        TorrentInfo(pieceSize, totalSize, rootHash, headerSize, headerHash, d.toString())
//                    }
//                    torrentInfoDeferred.complete(info)
//                }
//            }
        }
    }

    private fun peer(rldpConnection: RldpConnection): TorrentPeer {
        val id = rldpConnection.adnl.remoteNode.shortId
        peers[id]?.let { return it }
        val peer = TorrentPeer(this, id)
        peers.put(id, peer)?.cancel()
        return peer
    }

    suspend fun onQuery(query: OverlayQuery) {
        if (query.overlay.id.shortId != overlay.id.shortId) {
            return
        }
        val storageFunction = try {
            TL.Boxed.decodeFromByteString<StorageFunction>(query.input)
        } catch (e: Throwable) {
            return
        }
        when (storageFunction) {
            is StorageFunction.AddUpdate -> {
                val peer = peer(rldp.connection(query.peer.remoteNode))
                peer.addUpdate(storageFunction)
                query.respond(TL.Boxed.encodeToByteString(StorageFunction.Ok))
            }

            is StorageFunction.GetPiece -> {
                null
            }

            StorageFunction.GetTorrentInfo -> {
                val torrentInfo = torrentInfo.await()
                query.respond(TL.Boxed.encodeToByteString(torrentInfo))
            }

            is StorageFunction.Ping -> {
                query.respond(TL.Boxed.encodeToByteString(StorageFunction.Pong))
            }
        }
    }

}
