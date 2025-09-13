package org.ton.kotlin.storage

import kotlinx.io.bytestring.ByteString
import kotlin.concurrent.atomics.AtomicIntArray
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalAtomicApi::class)
class PiecesMask(
    val piecesCount: Int
) {
    private val data = AtomicIntArray((piecesCount + 31) / 32) { 0 }

    fun copy(): PiecesMask {
        val copy = PiecesMask(piecesCount)
        for (i in 0 until data.size) {
            copy.data.storeAt(i, data.loadAt(i))
        }
        return copy
    }

    fun set(piece: Int, value: Boolean = true) {
        if (piece !in 0 until piecesCount) throw IndexOutOfBoundsException("piece=$piece, size=$piecesCount")
        val wi = piece ushr 5
        val m = 1 shl (piece and 31)
        while (true) {
            val cur = data.loadAt(wi)
            val next = if (value) cur or m else cur and m.inv()
            if (cur == next) return
            if (data.compareAndSetAt(wi, cur, next)) return
        }
    }

    fun get(piece: Int): Boolean {
        if (piece !in 0 until piecesCount) throw IndexOutOfBoundsException("piece=$piece, size=$piecesCount")
        val wordIndex = piece ushr 5
        val bitMask = 1 shl (piece and 31)
        return (data.loadAt(wordIndex) and bitMask) != 0
    }

    override fun toString(): String {
        return buildString {
            append("PiecesMask(piecesCount=$piecesCount, pieces=[")
            for (i in 0 until piecesCount) {
                append(if (this@PiecesMask.get(i)) '1' else '0')
            }
            append("])")
        }
    }

    fun applySegment(piecesMask: ByteString, pieceOffset: Int) = apply {
        require(pieceOffset >= 0) { "pieceOffset must be >= 0" }
        if (piecesCount == 0) return@apply
        if (piecesMask.size == 0) return@apply
        if (pieceOffset >= piecesCount) return@apply

        val totalBits = piecesMask.size * 8
        val endPieceExclusive = minOf(piecesCount, pieceOffset + totalBits)

        var piece = pieceOffset
        var byteIndex = 0
        while (piece < endPieceExclusive && byteIndex < piecesMask.size) {
            val b = piecesMask[byteIndex].toInt() and 0xFF
            if (b == 0) {
                val skip = minOf(8, endPieceExclusive - piece)
                piece += skip
                byteIndex++
                continue
            }
            var bit = 0
            while (bit < 8 && piece < endPieceExclusive) {
                if (((b ushr bit) and 1) != 0) {
                    val wi = piece ushr 5
                    val m = 1 shl (piece and 31)
                    while (true) {
                        val cur = data.loadAt(wi)
                        val next = cur or m
                        if (cur == next) break
                        if (data.compareAndSetAt(wi, cur, next)) break
                    }
                }
                piece++
                bit++
            }
            byteIndex++
        }
    }

    suspend fun iterateSegments(bytesSizeAtMost: Int, block: suspend (ByteString) -> Unit) {
        require(bytesSizeAtMost > 0) { "bytesSizeAtMost must be > 0" }
        if (piecesCount <= 0) return

        val words = data.size
        val totalBytes = (piecesCount + 7) ushr 3
        val bitsInLast = piecesCount and 31
        val lastMask = if (bitsInLast == 0) -1 else (1 shl bitsInLast) - 1

        var produced = 0
        val buf = ByteArray(bytesSizeAtMost)
        var pos = 0

        suspend fun flush() {
            if (pos == 0) return
            if (pos == buf.size) block(ByteString(buf)) else block(ByteString(buf, 0, pos))
            pos = 0
        }

        var i = 0
        while (i < words && produced < totalBytes) {
            var word = data.loadAt(i)
            if (i == words - 1 && bitsInLast != 0) word = word and lastMask

            val remaining = totalBytes - produced
            val need = if (remaining < 4) remaining else 4

            var j = 3
            val skip = 4 - need
            while (j >= skip) {
                if (pos == buf.size) flush()
                buf[pos++] = ((word ushr (j shl 3)) and 0xFF).toByte()
                produced++
                j--
            }

            i++
        }

        flush()
    }
}
