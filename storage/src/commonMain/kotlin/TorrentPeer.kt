package org.ton.kotlin.storage

import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.io.readByteArray
import kotlinx.io.readByteString
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.bitstring.ByteWrappingBitString
import org.ton.kotlin.cell.BagOfCells
import org.ton.kotlin.overlay.OverlayFunction
import org.ton.kotlin.rldp.RldpConnection
import org.ton.kotlin.tl.TL
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.CoroutineContext
import kotlin.math.min
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val logger = KtorSimpleLogger("org.ton.kotlin.storage.TorrentPeer")

@OptIn(ExperimentalAtomicApi::class)
class TorrentPeer @OptIn(ExperimentalTime::class) constructor(
    val torrent: Torrent,
    val id: AdnlIdShort,
    val sessionId: Long = Random.nextLong(0, Long.MAX_VALUE),
    rldpConnection: RldpConnection? = null
) : CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = torrent.coroutineContext + job + CoroutineName("TorrentPeer")

    private val rldpConnection =
        AtomicReference<Deferred<RldpConnection>?>(rldpConnection?.let { CompletableDeferred(it) })

    private val queryPrefix = TL.Boxed.encodeToByteArray(OverlayFunction.Query(torrent.overlay.id))

    private val updateSeqno = AtomicInt(0)
    private val peerUpdateSeqno = AtomicInt(0)
    var peerSessionId: Long? = null

    private val storageStateFlow = MutableStateFlow(StorageState(false, false))

    init {
        logger.info("${debugStr()} start session")
    }

    val piecesMask = async(start = CoroutineStart.LAZY) {
        logger.info("$id start waiting torrent info for piece mask initialization")
        val torrentInfo = torrent.torrentInfo.await()
        val piecesMask = PiecesMask(torrentInfo.piecesCount)
        MutableStateFlow(piecesMask)
    }

    private val updateQueue = Channel<StorageUpdate>(Channel.UNLIMITED)
    private val updateQueueJob = launch(start = CoroutineStart.LAZY) {
//        logger.info("start waiting for state initialization")
        val piecesMaskFlow = piecesMask.await()
//        logger.info("start update queue loop")
        for (update in updateQueue) {
            when (update) {
                is StorageUpdate.Init -> {
                    storageStateFlow.update {
                        update.state
                    }
                    val torrentPieces = torrent.pieces.await()
                    piecesMaskFlow.update {
                        val mask = it.copy()
                        mask.applySegment(update.havePieces, update.havePiecesOffset)
                        val updatedPiecesCount =
                            min(mask.piecesCount - update.havePiecesOffset, update.havePieces.size * 8)
                        for (i in update.havePiecesOffset until updatedPiecesCount) {
                            val torrentPiece = torrentPieces[i]
                            if (mask.get(i)) {
                                torrentPiece.addPeer(this@TorrentPeer)
                            } else {
                                torrentPiece.removePeer(this@TorrentPeer)
                            }
                        }
                        mask
                    }
                }

                is StorageUpdate.HavePieces -> {
                    val torrentPieces = torrent.pieces.await()
                    update.pieceId.forEach { pieceId ->
                        torrentPieces[pieceId].addPeer(this@TorrentPeer)
                    }
                    piecesMaskFlow.update {
                        val mask = it.copy()
                        update.pieceId.forEach { pieceId ->
                            mask.set(pieceId, true)
                        }
                        mask
                    }
                }

                is StorageUpdate.UpdateState -> {
                    storageStateFlow.update {
                        update.state
                    }
                }
            }
        }
    }
    private val getTorrentInfoJob = launch {
        while (true) {
            if (!torrent.torrentInfo.isCompleted) {
                logger.debug { "${debugStr()}: start get torrent info" }
                val torrentInfo = withTimeoutOrNull(5.seconds) {
                    getTorrentInfo()
                }
                if (torrentInfo == null) {
                    delay(30.seconds)
                    continue
                }
                logger.debug { "${debugStr()}: got torrent info" }
                val boc = BagOfCells(torrentInfo.data)
                val cell = boc.loadCell(boc.getRootCell())
                val info = kotlinx.io.Buffer().run {
                    write(cell.bits.toByteArray())
                    val pieceSize = readInt()
                    val totalSize = readLong()
                    val rootHash = readByteString(32)
                    val headerSize = readLong()
                    val headerHash = readByteString(32)
                    val d = ByteWrappingBitString(readByteArray())
                    TorrentInfo(pieceSize, totalSize, rootHash, headerSize, headerHash, d.toString())
                }
                torrent.torrentInfo.complete(info)
                break
            }
        }
    }

    private val sessionInitializationJob = async(start = CoroutineStart.LAZY) {
        val piecesMaskStateFlow = torrent.piecesMask.await()
        var offset = 0
        var currentPiecesMask = piecesMaskStateFlow.value.copy()
        currentPiecesMask.iterateSegments(512) { segment ->
            updateInit(segment, offset, torrent.storageState.value)
            offset += segment.size * Byte.SIZE_BITS
        }
        currentPiecesMask
    }

    private val sendStateUpdateJob = launch {
        var currentPiecesMask = sessionInitializationJob.await()
        torrent.piecesMask.await().collect { newMask ->
            val newMask = newMask.copy()
            val newPieces = ArrayList<Int>()
            for (i in 0 until newMask.piecesCount) {
                if (newMask.get(i) && !currentPiecesMask.get(i)) {
                    newPieces.add(i)
                }
            }
            currentPiecesMask = newMask
        }
    }

    private val sendPingJob = launch {
        var fails = 0
        while (isActive) {
            logger.debug { "send ping... $sessionId" }
            val result = withTimeoutOrNull(5.seconds) {
                ping()
            }
            if (result == null) {
                fails++
                logger.debug { "failed to ping $sessionId, fails=$fails" }
            } else {
                fails = 0
                logger.debug { "pinged! $sessionId" }
            }
            delay(15.seconds)
        }
    }

    private suspend fun query(query: ByteArray): ByteArray {
        return connection().query(queryPrefix + query)
    }

    suspend fun ping(): StorageFunction.Pong {
        return TL.Boxed.decodeFromByteArray(
            StorageFunction.Pong.serializer(),
            connection().adnl.query(queryPrefix + TL.Boxed.encodeToByteArray(StorageFunction.Ping(sessionId)))
        )
    }

    suspend fun getPiece(pieceId: Int): StoragePiece {
        logger.debug { "$id start downloading piece: $pieceId" }
        return TL.Boxed.decodeFromByteArray(
            StoragePiece.serializer(),
            query(TL.Boxed.encodeToByteArray(StorageFunction.GetPiece(pieceId)))
        )
    }

    suspend fun getTorrentInfo(): StorageTorrentInfo {
        return TL.Boxed.decodeFromByteArray(
            StorageTorrentInfo.serializer(),
            query(TL.Boxed.encodeToByteArray(StorageFunction.GetTorrentInfo))
        )
    }

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun updateInit(
        havePieces: ByteString,
        havePiecesOffset: Int,
        state: StorageState
    ) {
        TL.Boxed.decodeFromByteArray(
            StorageFunction.Ok.serializer(),
            query(
                TL.Boxed.encodeToByteArray(
                    StorageFunction.AddUpdate(
                        sessionId,
                        updateSeqno.fetchAndAdd(1),
                        StorageUpdate.Init(
                            havePieces,
                            havePiecesOffset,
                            state
                        )
                    )
                )
            )
        )
    }

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun connection(): RldpConnection {
        rldpConnection.load()?.let { return it.await() }
        val deferred = async {
            withTimeout(15.seconds) {
                torrent.rldp.dial(id, torrent.adnlNodeResolver)
            }
        }
        deferred.invokeOnCompletion {
            if (it != null) {
                rldpConnection.store(null)
            }
        }
        val old = rldpConnection.compareAndExchange(null, deferred)
        if (old != null) {
            deferred.cancelAndJoin()
            return old.await()
        }
        return deferred.await()
    }

    fun addUpdate(update: StorageFunction.AddUpdate) {
        if (update.sessionId != sessionId) {
            logger.info("drop: invalid session: ${update.sessionId}, current: $sessionId")
            return
        }
        if (peerUpdateSeqno.load() >= update.seqno) {
            logger.info("drop: too new peer seqno: ${update.seqno}, current: ${peerUpdateSeqno.load()}")
            return
        }
        updateQueue.trySend(update.update)
        updateQueueJob.start()
    }

    override fun toString(): String {
        return "TorrentPeer($id $sessionId<->$peerSessionId)"
    }
}

internal fun TorrentPeer.debugStr() = "[${
    id.hash.toHexString().let {
        it.substring(0, 8) + ".." + it.substring(it.length - 8, it.length)
    }
} 0x${this.sessionId.toHexString()}]"
