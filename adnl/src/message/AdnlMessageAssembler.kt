package org.ton.kotlin.adnl.message

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.contentEquals
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.crypto.sha256
import org.ton.kotlin.tl.TL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

internal class AdnlMessageAssembler(
    private val maxTotalBytes: Int = 64 * 1024,
    private val ttl: Duration = 10.seconds
) {
    private val now = TimeSource.Monotonic
    private val transfers = Hash256Map<ByteString, Transfer>({ it })

    fun accept(part: AdnlMessage.Part): AdnlMessage? {
        val len = part.data.size
        if (part.totalSize !in 1..maxTotalBytes) return null
        if (part.offset < 0 || part.offset + len > part.totalSize) return null

        if (transfers.size > 16) {
            sweepExpired()
        }

        val tr = transfers[part.hash] ?: Transfer(part, now, ttl).also {
            transfers[part.hash] = it
        }

        if (tr.totalSize != part.totalSize) {
            transfers.remove(tr.hash)
            return null
        }

        val wrote = tr.gaps.fill(part.offset, part.offset + len) { dst, srcStart, n ->
            part.data.copyInto(tr.buffer, destinationOffset = dst, startIndex = srcStart, endIndex = srcStart + n)
        }
        if (wrote == 0) return null

        tr.deadline = now.markNow() + ttl

        if (tr.gaps.isEmpty()) {
            transfers.remove(tr.hash)
            if (tr.hash.contentEquals(sha256(tr.buffer))) {
                return try {
                    TL.Boxed.decodeFromByteArray<AdnlMessage>(tr.buffer)
                } catch (_: Throwable) {
                    null
                }
            }
        }
        return null
    }

    private fun sweepExpired() {
        val it = transfers.entries.iterator()
        while (it.hasNext()) {
            if (it.next().value.deadline.hasPassedNow()) {
                it.remove()
            }
        }
    }

    private class Transfer(
        val hash: ByteString,
        val totalSize: Int,
        val buffer: ByteArray,
        val gaps: Gaps,
        var deadline: TimeSource.Monotonic.ValueTimeMark
    ) {
        constructor(first: AdnlMessage.Part, now: TimeSource.Monotonic, ttl: Duration) : this(
            hash = first.hash,
            totalSize = first.totalSize,
            buffer = ByteArray(first.totalSize),
            gaps = Gaps(first.totalSize),
            deadline = now.markNow() + ttl
        )
    }

    private class Gaps(total: Int) {
        private val list = ArrayList<IntRange>(1).apply { add(0 until total) }
        fun isEmpty(): Boolean = list.isEmpty()

        fun fill(
            start: Int,
            end: Int,
            copy: (dstStart: Int, srcStart: Int, len: Int) -> Unit
        ): Int {
            if (start >= end || list.isEmpty()) return 0
            var written = 0
            var i = 0
            while (i < list.size) {
                val g = list[i]
                if (g.last + 1 <= start) {
                    i++
                    continue
                }
                if (g.first >= end) break

                val s = maxOf(g.first, start)
                val e = minOf(g.last + 1, end)
                if (s < e) {
                    val n = e - s
                    copy(s, s - start, n)
                    written += n

                    val left = if (g.first < s) g.first until s else null
                    val right = if (e <= g.last) e..g.last else null
                    when {
                        left != null && right != null -> {
                            list[i] = left
                            list.add(i + 1, right)
                            i++
                        }

                        left != null -> list[i] = left
                        right != null -> list[i] = right
                        else -> {
                            list.removeAt(i)
                            continue
                        }
                    }
                }
                i++
            }
            return written
        }
    }
}
