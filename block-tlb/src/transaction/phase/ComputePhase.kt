@file:Suppress("OPT_IN_USAGE", "PackageDirectoryMismatch")

package org.ton.kotlin.transaction.phase

import kotlinx.io.bytestring.ByteString
import org.ton.block.Coins
import org.ton.block.VarUInteger
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.loadRef
import org.ton.cell.storeRef
import org.ton.kotlin.cell.CellContext
import org.ton.sdk.bigint.toInt
import org.ton.sdk.bigint.toLong
import org.ton.tlb.NullableTlbCodec
import org.ton.tlb.TlbCodec

/**
 * Compute phase info.
 *
 * At this phase the VM is executed to produce a list of actions.
 *
 * @see [org.ton.kotlin.transaction.TransactionInfo]
 */
public sealed interface ComputePhase {
    public val skipReason: Skipped?

    /**
     * Skipped compute phase info.
     */
    public enum class Skipped : ComputePhase {
        /**
         * Contract doesn't have state to execute.
         */
        NoState,

        /**
         * Contract state is invalid.
         */
        BadState,

        /**
         * Not enough gas to execute compute phase.
         */
        NoGas,

        /**
         * Account was suspended by the config.
         */
        Suspended;

        override val skipReason: Skipped? get() = this
    }

    /**
     * Executed compute phase info.
     */
    public data class Executed(
        /**
         * Whether the execution was successful.
         */
        val isSuccess: Boolean,

        /**
         * Whether the `init` from the incoming message was used.
         */
        val isMsgStateUsed: Boolean,

        /**
         * Whether the account state changed to `Active` during this phase.
         */
        val isAccountActivated: Boolean,

        /**
         * Total amount of tokens spent to execute this phase.
         */
        val gasFees: Coins,

        /**
         * Amount of gas used by the VM to execute this phase.
         */
        val gasUsed: Long,

        /**
         * Max gas amount which could be used.
         */
        val gasLimit: Long,

        /**
         * Max gas amount which could be used before accepting this transaction.
         */
        val gasCredit: Int?,

        /**
         * Execution mode.
         */
        val mode: Byte,

        /**
         * VM exit code.
         */
        val exitCode: Int,

        /**
         * Additional VM exit argument.
         */
        val exitArg: Int?,

        /**
         * The number of VM steps it took to complete this phase.
         */
        val vmSteps: Int,

        /**
         * Hash of the initial state of the VM.
         */
        val vmInitStateHash: ByteString,

        /**
         * Hash of the VM state after executing this phase.
         */
        val vmFinalStateHash: ByteString,
    ) : ComputePhase {
        override val skipReason: Skipped? get() = null
    }

    public companion object : TlbCodec<ComputePhase> by ComputeSkipReasonTlbCodec
}

private object ComputeSkipReasonTlbCodec : TlbCodec<ComputePhase> {
    private val varUInt7 = VarUInteger.tlbCodec(7)
    private val maybeVarUInt3 = NullableTlbCodec(VarUInteger.tlbCodec(3))

    override fun loadTlb(slice: CellSlice, context: CellContext): ComputePhase {
        return when (val tag = slice.loadUInt(3).toInt()) {
            // tr_phase_compute_skipped$0 cskip_no_state$00
            0b000 -> ComputePhase.Skipped.NoState
            0b001 -> ComputePhase.Skipped.BadState
            0b010 -> ComputePhase.Skipped.NoGas
            0b011 -> { // tr_phase_compute_skipped$0 cskip_suspended$110
                if (!slice.loadBoolean()) ComputePhase.Skipped.Suspended
                else throw IllegalStateException("Invalid ComputeSkipReason tag: 111")
            }

            else -> {
                if (tag and 0b100 == 0) throw IllegalStateException("Invalid ComputeSkipReason tag: ${tag.toString(2)}")
                val isSuccess = (tag and 0b010) != 0
                val isMsgStateUsed = (tag and 0b001) != 0
                val isAccountActivated = slice.loadBoolean()
                val gasFees = Coins.loadTlb(slice, context)
                slice.loadRef(context) {
                    val gasUsed = varUInt7.loadTlb(this, context).value.toLong()
                    val gasLimit = varUInt7.loadTlb(this, context).value.toLong()
                    val gasCredit = maybeVarUInt3.loadTlb(this, context)?.value?.toInt()
                    val mode = loadInt(8).toInt().toByte()
                    val exitCode = loadInt(32).toInt()
                    val exitArg = if (loadBoolean()) loadInt(32).toInt() else null
                    val vmSteps = loadUInt(32).toInt()
                    val vmInitStateHash = loadByteString(32)
                    val vmFinalStateHash = loadByteString(32)
                    ComputePhase.Executed(
                        isSuccess,
                        isMsgStateUsed,
                        isAccountActivated,
                        gasFees,
                        gasUsed,
                        gasLimit,
                        gasCredit,
                        mode,
                        exitCode,
                        exitArg,
                        vmSteps,
                        vmInitStateHash,
                        vmFinalStateHash
                    )
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun storeTlb(builder: CellBuilder, value: ComputePhase, context: CellContext) {
        when (value) {
            ComputePhase.Skipped.NoState -> builder.storeUInt(0b000, 3)
            ComputePhase.Skipped.BadState -> builder.storeUInt(0b001, 3)
            ComputePhase.Skipped.NoGas -> builder.storeUInt(0b010, 3)
            ComputePhase.Skipped.Suspended -> builder.storeUInt(0b0110, 4)
            is ComputePhase.Executed -> {
                builder.storeBoolean(true)
                builder.storeBoolean(value.isSuccess)
                builder.storeBoolean(value.isMsgStateUsed)
                builder.storeBoolean(value.isAccountActivated)
                Coins.storeTlb(builder, value.gasFees)
                builder.storeRef(context) {
                    varUInt7.storeTlb(this, VarUInteger(value.gasUsed), context)
                    varUInt7.storeTlb(this, VarUInteger(value.gasLimit), context)
                    maybeVarUInt3.storeTlb(this, value.gasCredit?.let { VarUInteger(it) }, context)
                    storeInt(value.mode, 8)
                    storeInt(value.exitCode, 32)
                    if (value.exitArg != null) {
                        storeBoolean(true)
                        storeInt(value.exitArg, 32)
                    } else {
                        storeBoolean(false)
                    }
                    storeUInt(value.vmSteps, 32)
                    storeByteString(value.vmInitStateHash)
                    storeByteString(value.vmFinalStateHash)
                }
            }
        }
    }
}
