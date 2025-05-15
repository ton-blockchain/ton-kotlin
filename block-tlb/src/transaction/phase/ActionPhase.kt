@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.transaction.phase

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.block.AccStatusChange
import org.ton.kotlin.block.Coins
import org.ton.kotlin.block.StorageUsedShort
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.NullableTlbCodec
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.constructor.IntTlbConstructor

/**
 * Action phase info.
 *
 * At this phase, the list of actions from the compute phase
 * is converted into updates and outgoing messages.
 *
 * @see [org.ton.kotlin.transaction.TransactionInfo]
 */
public data class ActionPhase(
    /**
     * Whether the execution was successful.
     */
    val success: Boolean,

    /**
     *  Whether the action list was valid.
     */
    val isValid: Boolean,

    /**
     * There were no funds to create an outgoing message.
     */
    val noFunds: Boolean,

    /**
     * Account status change during execution of this phase.
     */
    val statusChange: AccStatusChange,

    /**
     * Total forwarding fee for outgoing messages.
     */
    val totalFwdFees: Coins?,

    /**
     * Total fees for processing all actions.
     */
    val totalActionFees: Coins?,

    /**
     * Result code of the phase.
     */
    val resultCode: Int,

    /**
     * Optional result argument of the phase.
     */
    val resultArg: Int?,

    /**
     * The total number of processed actions.
     */
    val totalActions: Int,

    /**
     * The number of special actions (`ReserveCurrency`, `SetCode`, `ChangeLibrary`, copyleft).
     */
    val specialActions: Int,

    /**
     * The number of skipped actions.
     */
    val skippedActions: Int,

    /**
     * The number of outgoing messages created by the compute phase.
     */
    val messagesCreated: Int,

    /**
     * The hash of the actions list.
     */
    val actionListHash: ByteString,

    /**
     * The total number of unique cells (bits / refs) of produced messages.
     */
    val totalMessageSize: StorageUsedShort
) {
    init {
        require(actionListHash.size == 32) { "expected actionListHash.size == 32, actual: ${actionListHash.size}" }
    }

    public companion object : TlbCodec<ActionPhase> by TrActionPhaseTlbCodec
}

private object TrActionPhaseTlbCodec : TlbCodec<ActionPhase> {
    val maybeCoins = NullableTlbCodec(Coins)
    val maybeInt32 = NullableTlbCodec(IntTlbConstructor.int(32))

    override fun storeTlb(
        builder: CellBuilder,
        value: ActionPhase,
        context: CellContext
    ) = builder {
        storeBoolean(value.success)
        storeBoolean(value.isValid)
        storeBoolean(value.noFunds)
        AccStatusChange.storeTlb(builder, value.statusChange, context)
        maybeCoins.storeTlb(builder, value.totalFwdFees, context)
        maybeCoins.storeTlb(builder, value.totalActionFees, context)
        storeInt(value.resultCode, 32)
        maybeInt32.storeTlb(builder, value.resultCode, context)
        storeUInt(value.totalActions, 16)
        storeUInt(value.specialActions, 16)
        storeUInt(value.skippedActions, 16)
        storeUInt(value.messagesCreated, 16)
        storeByteString(value.actionListHash)
        StorageUsedShort.storeTlb(builder, value.totalMessageSize, context)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): ActionPhase = slice {
        val success = loadBoolean()
        val isValid = loadBoolean()
        val noFunds = loadBoolean()
        val statusChange = AccStatusChange.loadTlb(slice, context)
        val totalFwdFees = maybeCoins.loadTlb(slice, context)
        val totalActionFees = maybeCoins.loadTlb(slice, context)
        val resultCode = loadInt(32).toInt()
        val resultArg = maybeInt32.loadTlb(slice, context)
        val totalActions = loadUInt(16).toInt()
        val specialActions = loadUInt(16).toInt()
        val skippedActions = loadUInt(16).toInt()
        val messagesCreated = loadUInt(16).toInt()
        val actionListHash = loadByteString(32)
        val totalMessageSize = StorageUsedShort.loadTlb(slice, context)
        ActionPhase(
            success,
            isValid,
            noFunds,
            statusChange,
            totalFwdFees,
            totalActionFees,
            resultCode,
            resultArg,
            totalActions,
            specialActions,
            skippedActions,
            messagesCreated,
            actionListHash,
            totalMessageSize
        )
    }
}
