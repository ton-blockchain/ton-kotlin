package org.ton.kotlin.overlay

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.AdnlPeerPair
import kotlin.time.Duration
import kotlin.time.measureTimedValue
import kotlin.time.times

class OverlayPeer internal constructor(
    nodeInfo: OverlayNodeInfo,
    val adnlPeerPair: AdnlPeerPair,
) {
    var nodeInfo = nodeInfo
        private set
    var rtt: Duration = Duration.INFINITE
        private set

    suspend fun AdnlPeerPair.rawQuery(rawQuery: ByteString): ByteString {
        val (rawAnswer, sample) = measureTimedValue {
            query(rawQuery)
        }
        rtt = ewma(rtt, sample, 0.2)
        return rawAnswer
    }

    private fun ewma(prev: Duration, sample: Duration, alpha: Double) =
        if (prev.isInfinite()) sample else alpha * sample + (1 - alpha) * prev
}
