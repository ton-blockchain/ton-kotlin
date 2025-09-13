package org.ton.kotlin.storage

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
data class TorrentInfo(
    val pieceSize: Int,
    val totalSize: Long,
    @Serializable(ByteStringBase64Serializer::class)
    val rootHash: ByteString,
    val headerSize: Long,
    @Serializable(ByteStringBase64Serializer::class)
    val headerHash: ByteString,
    val description: String
) {
    val piecesCount: Int get() = ((totalSize + pieceSize - 1) / pieceSize).toInt()
    val headerPiecesCount: Int get() = ((headerSize + pieceSize - 1) / pieceSize).toInt()

    override fun toString(): String = buildString {
        append("TorrentInfo(pieceSize=")
        append(pieceSize)
        append(", totalSize=")
        append(totalSize)
        append(", headerSize=")
        append(headerSize)
        append(", description='")
        append(description)
        append("', piecesCount=")
        append(piecesCount)
        append(", headerPiecesCount=")
        append(headerPiecesCount)
        append(", rootHash=")
        append(rootHash.toHexString())
        append(", headerHash=")
        append(headerHash.toHexString())
        append(")")
    }
}
