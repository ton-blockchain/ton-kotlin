package org.ton.kotlin.account

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

/**
 * State of an existing [Account].
 *
 * @see [Account]
 */
public sealed interface AccountState {
    /**
     * Account status.
     */
    public val status: AccountStatus

    public val stateInit: StateInit? get() = null

    /**
     * Account exists but has not yet been deployed.
     */
    public object Uninit : AccountState {
        override val status: AccountStatus get() = AccountStatus.UNINIT
    }

    /**
     * Account exists and has been deployed.
     */
    public data class Active(
        override val stateInit: StateInit,
    ) : AccountState {
        override val status: AccountStatus get() = AccountStatus.ACTIVE
    }

    /**
     * Account exists but has been frozen. Contains a hash of the last known [StateInit].
     */
    public data class Frozen(
        val stateInitHash: ByteString,
    ) : AccountState {
        init {
            require(stateInitHash.size == 32) { "required: stateInitHash.size == 32, actual: ${stateInitHash.size}" }
        }

        override val status: AccountStatus get() = AccountStatus.FROZEN
    }

    public companion object : TlbCodec<AccountState> by AccountStateTlbCodec
}

private object AccountStateTlbCodec : TlbCodec<AccountState> {
    override fun loadTlb(slice: CellSlice, context: CellContext): AccountState {
        return if (slice.loadBoolean()) {
            AccountState.Active(StateInit.loadTlb(slice, context))
        } else if (slice.loadBoolean()) {
            AccountState.Frozen(slice.loadByteString(32))
        } else {
            AccountState.Uninit
        }
    }

    override fun storeTlb(builder: CellBuilder, value: AccountState, context: CellContext) {
        when (value) {
            is AccountState.Active -> {
                builder.storeBoolean(true)
                StateInit.storeTlb(builder, value.stateInit, context)
            }

            is AccountState.Frozen -> {
                builder.storeUInt(0b01, 2)
                builder.storeByteString(value.stateInitHash)
            }

            AccountState.Uninit -> {
                builder.storeUInt(0b00, 2)
            }
        }
    }
}
