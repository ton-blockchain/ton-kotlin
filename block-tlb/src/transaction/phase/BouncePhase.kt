@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.transaction.phase

import org.ton.kotlin.block.Coins
import org.ton.kotlin.block.StorageUsedShort
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * Bounce phase info.
 *
 * At this phase, some funds are returned to the sender.
 *
 * @see [org.ton.kotlin.transaction.TransactionInfo]
 */
public sealed interface BouncePhase {
    public val forwardFees: Coins

    /**
     * Skipped bounce phase info.
     */
    public data class NoFunds(
        /**
         * The total number of unique cells (bits / refs) of the bounced message.
         */
        val msgSize: StorageUsedShort,

        /**
         * Required amount of coins to send the bounced message.
         */
        override val forwardFees: Coins
    ) : BouncePhase

    /**
     * Bounce phase was executed.
     */
    public data class Executed(
        /**
         * The total number of unique cells (bits / refs) of the bounced message.
         */
        val msgSize: StorageUsedShort = StorageUsedShort.Companion.ZERO,

        /**
         * The part of fees for the validators.
         */
        val forwardFeesCollected: Coins = Coins.Companion.ZERO,

        /**
         * Message forwarding fee.
         */
        override val forwardFees: Coins = Coins.Companion.ZERO,
    ) : BouncePhase

    public companion object : TlbCodec<BouncePhase> by BouncePhaseCodec
}

private object BouncePhaseCodec : TlbCodec<BouncePhase> {
    private val ZERO = BouncePhase.Executed()

    override fun storeTlb(builder: CellBuilder, value: BouncePhase, context: CellContext) {
        when (value) {
            is BouncePhase.Executed -> { // tr_phase_bounce_ok$1
                builder.storeBoolean(true)
                StorageUsedShort.storeTlb(builder, value.msgSize, context)
                Coins.storeTlb(builder, value.forwardFeesCollected, context)
                Coins.storeTlb(builder, value.forwardFees, context)
            }

            is BouncePhase.NoFunds -> { // tr_phase_bounce_nofunds$01
                builder.storeUInt(0b01, 2)
                StorageUsedShort.storeTlb(builder, value.msgSize, context)
                Coins.storeTlb(builder, value.forwardFees, context)
            }
        }
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): BouncePhase {
        when (val tag = slice.preloadUInt(2).toInt()) {
            0b00 -> {
                slice.skipBits(2)
                return ZERO
            }

            0b01 -> { // tr_phase_bounce_nofunds$01
                slice.skipBits(2)
                val msgSize = StorageUsedShort.loadTlb(slice, context)
                val forwardFees = Coins.loadTlb(slice, context)
                return BouncePhase.NoFunds(msgSize, forwardFees)
            }

            0b10, 0b11 -> { // tr_phase_bounce_ok$1
                slice.skipBits(1)
                val msgSize = StorageUsedShort.loadTlb(slice, context)
                val forwardFees = Coins.loadTlb(slice, context)
                return BouncePhase.Executed(msgSize, forwardFees)
            }

            else -> throw IllegalArgumentException("Invalid bounce phase tag: ${tag.toString(2)}")
        }
    }

}
