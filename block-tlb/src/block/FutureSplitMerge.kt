@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@JsonClassDiscriminator("@type")
public sealed interface FutureSplitMerge : TlbObject {

    @SerialName("fsm_none")
    public object FsmNone : FutureSplitMerge,
        TlbConstructorProvider<FsmNone> by FutureSplitMergeNoneTlbConstructor {
        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("fsm_none")

        override fun toString(): String = print().toString()
    }


    @SerialName("fsm_merge")
    public data class FsmMerge(
        val mergeUtime: UInt,
        val interval: UInt
    ) : FutureSplitMerge {
        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("fsm_merge") {
                field("merge_utime", mergeUtime)
                field("interval", interval)
            }
        }

        override fun toString(): String = print().toString()

        public companion object : TlbConstructorProvider<FsmMerge> by FutureSplitMergeMergeTlbConstructor
    }


    @SerialName("fsm_split")
    public data class FsmSplit(
        val splitUtime: UInt,
        val interval: UInt
    ) : FutureSplitMerge {
        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("fsm_split") {
                field("split_utime", splitUtime)
                field("interval", interval)
            }
        }

        override fun toString(): String = print().toString()

        public companion object : TlbConstructorProvider<FsmSplit> by FsmSplitTlbConstructor
    }

    public companion object : TlbCombinatorProvider<FutureSplitMerge> by FutureSplitMergeTlbCombinator
}

private object FutureSplitMergeTlbCombinator : TlbCombinator<FutureSplitMerge>(
    FutureSplitMerge::class,
    FutureSplitMerge.FsmNone::class to FutureSplitMerge.FsmNone,
    FutureSplitMerge.FsmSplit::class to FutureSplitMerge.FsmSplit,
    FutureSplitMerge.FsmMerge::class to FutureSplitMerge.FsmMerge,
)

private object FutureSplitMergeNoneTlbConstructor : TlbConstructor<FutureSplitMerge.FsmNone>(
    schema = "fsm_none\$0 = FutureSplitMerge;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: FutureSplitMerge.FsmNone) = Unit
    override fun loadTlb(cellSlice: CellSlice): FutureSplitMerge.FsmNone = FutureSplitMerge.FsmNone
}

private object FsmSplitTlbConstructor : TlbConstructor<FutureSplitMerge.FsmSplit>(
    schema = "fsm_split\$10 split_utime:uint32 interval:uint32 = FutureSplitMerge;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: FutureSplitMerge.FsmSplit
    ) = cellBuilder {
        storeUInt32(value.splitUtime)
        storeUInt32(value.interval)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): FutureSplitMerge.FsmSplit = cellSlice {
        val splitUtime = loadUInt32()
        val interval = loadUInt32()
        FutureSplitMerge.FsmSplit(splitUtime, interval)
    }
}


private object FutureSplitMergeMergeTlbConstructor : TlbConstructor<FutureSplitMerge.FsmMerge>(
    schema = "fsm_merge\$11 merge_utime:uint32 interval:uint32 = FutureSplitMerge;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: FutureSplitMerge.FsmMerge
    ) = cellBuilder {
        storeUInt32(value.mergeUtime)
        storeUInt32(value.interval)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): FutureSplitMerge.FsmMerge = cellSlice {
        val mergeUtime = loadUInt32()
        val interval = loadUInt32()
        FutureSplitMerge.FsmMerge(mergeUtime, interval)
    }
}
