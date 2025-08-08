package org.ton.kotlin.adnl

const val HISTORY_BITS = 512
const val HISTORY_SIZE = HISTORY_BITS / 64
const val INDEX_MASK = (HISTORY_BITS / 2 - 1).toLong() // 0xFF for 512 bits

internal class HistoryBits {
    var index: Long = 0L
    val bits = LongArray(HISTORY_SIZE) { 0L }
}

internal class PacketsHistory private constructor(
    private val mask: HistoryBits?,
    private var seqno: Long = 0
) {
    companion object {
        fun forSender(): PacketsHistory = PacketsHistory(mask = null)
        fun forReceiver(): PacketsHistory = PacketsHistory(mask = HistoryBits())
    }

    fun reset() {
        mask?.let {
            it.index = 0L
            for (i in it.bits.indices) {
                it.bits[i] = if (i == HISTORY_SIZE / 2) 1L else 0L
            }
        }
        seqno = 0L
    }

    fun seqno(): Long = seqno

    fun incrementSeqno(): Long {
        seqno += 1
        return seqno
    }

    fun deliverPacket(seqno: Long): Boolean {
        val mask = this.mask ?: run {
            if (this.seqno < seqno) this.seqno = seqno
            return true
        }

        val seqnoMasked = seqno and INDEX_MASK
        val seqnoNormalized = seqno and INDEX_MASK.inv()

        val index = mask.index
        val indexMasked = index and INDEX_MASK
        val indexNormalized = index and INDEX_MASK.inv()

        if (indexNormalized > seqnoNormalized + INDEX_MASK + 1) {
            println("DEBUG: peer packet is too old: seqno=$seqno indexNormalized=$indexNormalized")
            return false
        }

        val maskBit = 1L shl (seqnoMasked % 64).toInt()
        val maskOffset = when {
            indexNormalized > seqnoNormalized -> 0
            indexNormalized == seqnoNormalized -> HISTORY_SIZE / 2
            else -> null
        }

        val nextIndex = when (maskOffset) {
            null -> {
                if (indexNormalized + INDEX_MASK + 1 == seqnoNormalized) {
                    // slide window
                    for (i in 0 until HISTORY_SIZE / 2) {
                        mask.bits[i] = mask.bits[i + HISTORY_SIZE / 2]
                    }
                    for (i in HISTORY_SIZE / 2 until HISTORY_SIZE) {
                        mask.bits[i] = 0L
                    }
                } else {
                    for (i in mask.bits.indices) {
                        mask.bits[i] = 0L
                    }
                }
                seqnoNormalized
            }

            else -> {
                val offset = maskOffset + (seqnoMasked / 64).toInt()
                val alreadyDelivered = mask.bits[offset] and maskBit
                if (alreadyDelivered != 0L) {
                    println("TRACE: peer packet was already received: seqno=$seqno")
                    return false
                }

                mask.bits[offset] = mask.bits[offset] or maskBit
                indexNormalized
            }
        }

        if (this.seqno < seqno) {
            this.seqno = seqno
        }

        val newIndex = (indexMasked + 1) and INDEX_MASK.inv()
        mask.index = nextIndex or newIndex

        return true
    }
}
