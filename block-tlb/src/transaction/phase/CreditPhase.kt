@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.transaction.phase

import org.ton.kotlin.block.Coins
import org.ton.kotlin.block.CurrencyCollection
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.NullableTlbCodec
import org.ton.kotlin.tlb.TlbCodec

/**
 * Credit phase info.
 *
 * At this phase message balance is added to the account balance.
 *
 * @see [org.ton.kotlin.transaction.TransactionInfo]
 */
public data class CreditPhase(
    /**
     * Amount of coins paid for the debt.
     */
    val dueFeesCollected: Coins?,

    /**
     * Amount of tokens added to the account balance from the remaining message balance.
     */
    val credit: CurrencyCollection
) {
    public companion object : TlbCodec<CreditPhase> by CreditPhaseTlbCodec
}

private object CreditPhaseTlbCodec : TlbCodec<CreditPhase> {
    val maybeCoins = NullableTlbCodec(Coins)

    override fun storeTlb(
        builder: CellBuilder,
        value: CreditPhase,
        context: CellContext
    ) {
        maybeCoins.storeTlb(builder, value.dueFeesCollected, context)
        CurrencyCollection.storeTlb(builder, value.credit, context)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): CreditPhase {
        val dueFeesCollected = maybeCoins.loadTlb(slice, context)
        val credit = CurrencyCollection.loadTlb(slice, context)
        return CreditPhase(dueFeesCollected, credit)
    }
}
