package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbCombinator
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbStorer


public enum class AccStatusChange {
    @SerialName("acst_unchanged")
    UNCHANGED {
        override fun toString(): String = "acst_unchanged"
    }, // x -> x

    @SerialName("acst_frozen")
    FROZEN {
        override fun toString(): String = "acst_frozen"
    }, // init -> frozen

    @SerialName("acst_deleted")
    DELETED {
        override fun toString(): String = "acst_deleted"
    } // frozen -> deleted
    ;

    public companion object : TlbCodec<AccStatusChange> by AccStatusChangeTlbCombinator
}

private object AccStatusChangeTlbCombinator : TlbCombinator<AccStatusChange>(
    AccStatusChange::class,
    AccStatusChange::class to AccStatusChangeUnchangedTlbConstructor,
    AccStatusChange::class to AccStatusChangeFrozenTlbConstructor,
    AccStatusChange::class to AccStatusChangeDeletedTlbConstructor,
) {
    override fun findTlbStorerOrNull(value: AccStatusChange): TlbStorer<AccStatusChange>? {
        return when (value) {
            AccStatusChange.UNCHANGED -> AccStatusChangeUnchangedTlbConstructor
            AccStatusChange.FROZEN -> AccStatusChangeFrozenTlbConstructor
            AccStatusChange.DELETED -> AccStatusChangeDeletedTlbConstructor
        }
    }
}

private object AccStatusChangeUnchangedTlbConstructor : TlbConstructor<AccStatusChange>(
    schema = "acst_unchanged\$0 = AccStatusChange;"
) {
    override fun storeTlb(builder: CellBuilder, value: AccStatusChange) {
    }

    override fun loadTlb(slice: CellSlice): AccStatusChange = AccStatusChange.UNCHANGED
}

private object AccStatusChangeFrozenTlbConstructor : TlbConstructor<AccStatusChange>(
    schema = "acst_frozen\$10 = AccStatusChange;"
) {
    override fun storeTlb(builder: CellBuilder, value: AccStatusChange) {
    }

    override fun loadTlb(slice: CellSlice): AccStatusChange = AccStatusChange.FROZEN
}

private object AccStatusChangeDeletedTlbConstructor : TlbConstructor<AccStatusChange>(
    schema = "acst_deleted\$11 = AccStatusChange;"
) {
    override fun storeTlb(builder: CellBuilder, value: AccStatusChange) {
    }

    override fun loadTlb(slice: CellSlice): AccStatusChange = AccStatusChange.DELETED
}
