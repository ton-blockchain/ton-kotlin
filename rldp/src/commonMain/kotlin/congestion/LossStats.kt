package org.ton.kotlin.rldp.congestion

internal class LossStats {
    var loss = 0.0
    var ackSum = 0
    var lostSum = 0

    fun onUpdate(ackedCount: Int, lostCount: Int) {
        ackSum += ackedCount
        lostSum += lostCount
        val total = ackSum + lostSum
        if (total > 1000) {
            val newLoss = (lostSum.toDouble() / total).coerceIn(0.001, 0.2)
            loss = newLoss
            ackSum = 0
            lostSum = 0
        }
    }
}
