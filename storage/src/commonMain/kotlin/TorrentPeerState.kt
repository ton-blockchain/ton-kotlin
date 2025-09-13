package org.ton.kotlin.storage

typealias PartId = Int

data class TorrentPeerState(
    val piecesMask: PiecesMask,
    val state: StorageState
)
