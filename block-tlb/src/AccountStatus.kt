package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider


public enum class AccountStatus {
    @SerialName("acc_state_uninit")
    UNINIT {
        override fun toString(): String = "acc_state_uninit"
    },

    @SerialName("acc_state_frozen")
    FROZEN {
        override fun toString(): String = "acc_state_frozen"
    },

    @SerialName("acc_state_active")
    ACTIVE {
        override fun toString(): String = "acc_state_active"
    },

    @SerialName("acc_state_nonexist")
    NONEXIST {
        override fun toString(): String = "acc_state_nonexist"
    };

    public companion object : TlbCombinatorProvider<AccountStatus> by AccountStatusTlbCombinator
}

private object AccountStatusTlbCombinator : TlbCombinator<AccountStatus>(
    AccountStatus::class,
    AccountStatus::class to AccountStatusUninitTlbConstructor,
    AccountStatus::class to AccountStatusFrozenTlbConstructor,
    AccountStatus::class to AccountStatusActiveTlbConstructor,
    AccountStatus::class to AccountStatusNonExistTlbConstructor,
) {
    override fun findTlbStorerOrNull(value: AccountStatus): TlbConstructor<AccountStatus>? {
        return when (value) {
            AccountStatus.UNINIT -> AccountStatusUninitTlbConstructor
            AccountStatus.FROZEN -> AccountStatusFrozenTlbConstructor
            AccountStatus.ACTIVE -> AccountStatusActiveTlbConstructor
            AccountStatus.NONEXIST -> AccountStatusNonExistTlbConstructor
        }
    }
}

private object AccountStatusUninitTlbConstructor : TlbConstructor<AccountStatus>(
    schema = "acc_state_uninit$00 = AccountStatus;"
) {
    override fun storeTlb(builder: CellBuilder, value: AccountStatus) {
    }

    override fun loadTlb(slice: CellSlice): AccountStatus = AccountStatus.UNINIT
}

private object AccountStatusFrozenTlbConstructor : TlbConstructor<AccountStatus>(
    schema = "acc_state_frozen$01 = AccountStatus;"
) {
    override fun storeTlb(builder: CellBuilder, value: AccountStatus) {
    }

    override fun loadTlb(slice: CellSlice): AccountStatus = AccountStatus.FROZEN
}

private object AccountStatusActiveTlbConstructor : TlbConstructor<AccountStatus>(
    schema = "acc_state_active$10 = AccountStatus;"
) {
    override fun storeTlb(builder: CellBuilder, value: AccountStatus) {
    }

    override fun loadTlb(slice: CellSlice): AccountStatus = AccountStatus.ACTIVE
}

private object AccountStatusNonExistTlbConstructor : TlbConstructor<AccountStatus>(
    schema = "acc_state_nonexist$11 = AccountStatus;"
) {
    override fun storeTlb(builder: CellBuilder, value: AccountStatus) {
    }

    override fun loadTlb(slice: CellSlice): AccountStatus = AccountStatus.NONEXIST
}
